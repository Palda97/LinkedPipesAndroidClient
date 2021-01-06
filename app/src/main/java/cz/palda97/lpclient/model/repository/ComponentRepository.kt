package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ComponentRetrofit
import cz.palda97.lpclient.model.network.ComponentRetrofit.Companion.componentRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import kotlinx.coroutines.*

@Suppress("NAME_SHADOWING")
class ComponentRepository(
    private val serverDao: ServerInstanceDao,
    private val pipelineDao: PipelineDao,
    private val sharedPreferences: SharedPreferences
) {

    enum class StatusCode {
        NO_CONNECT, INTERNAL_ERROR, SERVER_NOT_FOUND, DOWNLOADING_ERROR, PARSING_ERROR, OK, DOWNLOAD_IN_PROGRESS;

        companion object {
            val String.toStatus
                get() = try {
                    valueOf(this)
                } catch (e: IllegalArgumentException) {
                    INTERNAL_ERROR
                }
        }
    }

    private tailrec suspend fun Component.getRootTemplateId(): String {
        val template = pipelineDao.findTemplateById(templateId) ?: return templateId
        return Component(0, 0, template).getRootTemplateId()
    }

    /*private tailrec fun Component.getRootTemplateId(templates: List<Template>): String {
        val template = templates.find {
            it.id == templateId
        } ?: return templateId
        return Component(0, 0, template).getRootTemplateId(templates)
    }*/

    private suspend fun getComponentRetrofit(server: ServerInstance): Either<StatusCode, ComponentRetrofit> =
        try {
            Either.Right(RetrofitHelper.getBuilder(server, server.frontendUrl).componentRetrofit)
        } catch (e: IllegalArgumentException) {
            l("getComponentRetrofit ${e.toString()}")
            Either.Left(StatusCode.NO_CONNECT)
        }

    private suspend fun getComponentRetrofit(): Either<StatusCode, ComponentRetrofit> {
        val serverId = sharedPreferences.getLong(PipelineRepository.PIPELINE_SERVER_ID, 0)
        val server = serverDao.findById(serverId) ?: return Either.Left(StatusCode.INTERNAL_ERROR)
        return getComponentRetrofit(server)
    }

    private suspend fun downloadConfigInputs(
        component: Component,
        retrofit: ComponentRetrofit? = null
    ): Either<StatusCode, List<ConfigInput>> {
        val retrofit = retrofit ?: when (val res = getComponentRetrofit()) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val templateId = component.getRootTemplateId()
        val call = retrofit.dialog(templateId)
        val text = RetrofitHelper.getStringFromCall(call)
            ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = ConfigInputFactory(text, component.id)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private suspend fun downloadDialogJs(
        component: Component,
        retrofit: ComponentRetrofit? = null
    ): Either<StatusCode, DialogJs> {
        val retrofit = retrofit ?: when (val res = getComponentRetrofit()) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val templateId = component.getRootTemplateId()
        val call = retrofit.dialogJs(templateId)
        val text = RetrofitHelper.getStringFromCall(call)
            ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = DialogJsFactory(text, component.id)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private suspend fun downloadBindings(
        component: Component,
        retrofit: ComponentRetrofit? = null
    ): Either<StatusCode, List<Binding>> {
        val retrofit = retrofit ?: when (val res = getComponentRetrofit()) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val templateId = component.getRootTemplateId()
        val call = retrofit.bindings(templateId)
        val text = RetrofitHelper.getStringFromCall(call)
            ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = BindingFactory(text)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private suspend fun downloadConfigInputs(
        components: List<Component>,
        retrofit: ComponentRetrofit? = null
    ) = coroutineScope<List<Pair<Component, Either<StatusCode, List<ConfigInput>>>>> {
        val jobs = components.map {
            async {
                it to downloadConfigInputs(it, retrofit)
            }
        }
        jobs.map {
            it.await()
        }
    }

    private suspend fun downloadDialogJs(
        components: List<Component>,
        retrofit: ComponentRetrofit? = null
    ) = coroutineScope<List<Pair<Component, Either<StatusCode, DialogJs>>>> {
        val jobs = components.map {
            async {
                it to downloadDialogJs(it, retrofit)
            }
        }
        jobs.map {
            it.await()
        }
    }

    private suspend fun downloadBindings(
        components: List<Component>,
        retrofit: ComponentRetrofit? = null
    ) = coroutineScope<List<Pair<Component, Either<StatusCode, List<Binding>>>>> {
        val jobs = components.map {
            async {
                it to downloadBindings(it, retrofit)
            }
        }
        jobs.map {
            it.await()
        }
    }

    private suspend fun persistStatus(status: ConfigDownloadStatus) {
        pipelineDao.insertStatus(status)
    }

    private suspend fun persistStatus(list: List<ConfigDownloadStatus>) {
        pipelineDao.insertStatus(list)
    }

    private suspend fun persistConfigInput(list: List<ConfigInput>) {
        pipelineDao.insertConfigInput(list)
    }

    private suspend fun cacheConfigInput(component: Component) {
        val type = ConfigDownloadStatus.TYPE_CONFIG_INPUT
        persistStatus(ConfigDownloadStatus(component.id, type, StatusCode.DOWNLOAD_IN_PROGRESS))
        val status = ConfigDownloadStatus(component.id, type, when(val res = downloadConfigInputs(component)) {
            is Either.Left -> res.value
            is Either.Right -> {
                persistConfigInput(res.value)
                StatusCode.OK
            }
        })
        persistStatus(status)
    }

    private suspend fun cacheConfigInput(components: List<Component>) {
        val type = ConfigDownloadStatus.TYPE_CONFIG_INPUT
        persistStatus(components.map {
            ConfigDownloadStatus(it.id, type, StatusCode.DOWNLOAD_IN_PROGRESS)
        })
        val list = downloadConfigInputs(components)
        val statuses = list.map {
            val (component, either) = it
            ConfigDownloadStatus(component.id, type, when(either) {
                is Either.Left -> either.value
                is Either.Right -> {
                    persistConfigInput(either.value)
                    StatusCode.OK
                }
            })
        }
        persistStatus(statuses)
    }

    private suspend fun persistDialogJs(dialogJs: DialogJs) {
        pipelineDao.insertDialogJs(dialogJs)
    }

    private suspend fun cacheDialogJs(component: Component) {
        val type = ConfigDownloadStatus.TYPE_DIALOG_JS
        persistStatus(ConfigDownloadStatus(component.id, type, StatusCode.DOWNLOAD_IN_PROGRESS))
        val status = ConfigDownloadStatus(component.id, type, when(val res = downloadDialogJs(component)) {
            is Either.Left -> res.value
            is Either.Right -> {
                persistDialogJs(res.value)
                StatusCode.OK
            }
        })
        persistStatus(status)
    }

    private suspend fun cacheDialogJs(components: List<Component>) {
        val type = ConfigDownloadStatus.TYPE_DIALOG_JS
        persistStatus(components.map {
            ConfigDownloadStatus(it.id, type, StatusCode.DOWNLOAD_IN_PROGRESS)
        })
        val list = downloadDialogJs(components)
        val statuses = list.map {
            val (component, either) = it
            ConfigDownloadStatus(component.id, type, when(either) {
                is Either.Left -> either.value
                is Either.Right -> {
                    persistDialogJs(either.value)
                    StatusCode.OK
                }
            })
        }
        persistStatus(statuses)
    }

    private suspend fun persistBinding(list: List<Binding>) {
        pipelineDao.insertBinding(list)
    }

    private suspend fun cacheBinding(component: Component) {
        val type = ConfigDownloadStatus.TYPE_BINDING
        persistStatus(ConfigDownloadStatus(component.id, type, StatusCode.DOWNLOAD_IN_PROGRESS))
        val status = ConfigDownloadStatus(component.id, type, when(val res = downloadBindings(component)) {
            is Either.Left -> res.value
            is Either.Right -> {
                persistBinding(res.value)
                StatusCode.OK
            }
        })
        persistStatus(status)
    }

    private suspend fun cacheBinding(components: List<Component>) {
        val type = ConfigDownloadStatus.TYPE_BINDING
        persistStatus(components.map {
            ConfigDownloadStatus(it.id, type, StatusCode.DOWNLOAD_IN_PROGRESS)
        })
        val list = downloadBindings(components)
        val statuses = list.map {
            val (component, either) = it
            ConfigDownloadStatus(component.id, type, when(either) {
                is Either.Left -> either.value
                is Either.Right -> {
                    persistBinding(either.value)
                    StatusCode.OK
                }
            })
        }
        persistStatus(statuses)
    }

    suspend fun cache(components: List<Component>) = coroutineScope {
        val list = listOf(
            async { cacheConfigInput(components) },
            async { cacheDialogJs(components) },
            async { cacheBinding(components) }
        )
        list.forEach {
            it.await()
        }
    }

    suspend fun cache(component: Component) = coroutineScope {
        val statusConfigInput = pipelineDao.findStatus(component.id, ConfigDownloadStatus.TYPE_CONFIG_INPUT)
        val statusDialogJs = pipelineDao.findStatus(component.id, ConfigDownloadStatus.TYPE_CONFIG_INPUT)
        val statusBinding = pipelineDao.findStatus(component.id, ConfigDownloadStatus.TYPE_CONFIG_INPUT)
        val list = mutableListOf<Deferred<Unit>>()
        if (statusConfigInput == null || (statusConfigInput.result != StatusCode.OK.name && statusConfigInput.result != StatusCode.DOWNLOAD_IN_PROGRESS.name))
            list.add(async { cacheConfigInput(component) })
        if (statusDialogJs == null || (statusDialogJs.result != StatusCode.OK.name && statusDialogJs.result != StatusCode.DOWNLOAD_IN_PROGRESS.name))
            list.add(async { cacheDialogJs(component) })
        if (statusBinding == null || (statusBinding.result != StatusCode.OK.name && statusBinding.result != StatusCode.DOWNLOAD_IN_PROGRESS.name))
            list.add(async { cacheBinding(component) })
        list.forEach {
            it.await()
        }
    }

    var currentComponentId = ""

    fun liveConfigInput(componentId: String = currentComponentId) = pipelineDao.liveConfigWithStatus(componentId)
    fun liveDialogJs(componentId: String = currentComponentId) = pipelineDao.liveDialogJsWithStatus(componentId)
    fun liveBinding(componentId: String = currentComponentId) = pipelineDao.liveBindingWithStatus(componentId)

    val liveComponent
        get() = pipelineDao.liveComponentById(currentComponentId)

    suspend fun currentComponent() = pipelineDao.findComponentById(currentComponentId)
    suspend fun currentConfiguration() = pipelineDao.findConfigurationByComponentId(currentComponentId)

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
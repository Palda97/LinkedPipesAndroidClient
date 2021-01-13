package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ComponentRetrofit
import cz.palda97.lpclient.model.network.ComponentRetrofit.Companion.componentRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputComplete
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputContext
import cz.palda97.lpclient.viewmodel.editcomponent.OnlyStatus
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
            val String?.toStatus
                get() = if (this == null) {
                    INTERNAL_ERROR
                } else {
                    try {
                        valueOf(this)
                    } catch (e: IllegalArgumentException) {
                        INTERNAL_ERROR
                    }
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

    private var currentComponentId = ""

    val liveComponent
        get() = pipelineDao.liveComponentById(currentComponentId)

    private fun liveConfigInput(componentId: String = currentComponentId) = pipelineDao.liveConfigWithStatus(componentId)
    private fun liveDialogJs(componentId: String = currentComponentId) = pipelineDao.liveDialogJsWithStatus(componentId)
    private fun liveBinding(componentId: String = currentComponentId) = pipelineDao.liveBindingWithStatus(componentId)


    private suspend fun currentComponent() = pipelineDao.findComponentById(currentComponentId)
    //private suspend fun currentConfiguration() = pipelineDao.findConfigurationByComponentId(currentComponentId)

    //val configurationPersistRepo = PersistRepository(::currentConfiguration, pipelineDao::insertConfiguration)
    val componentPersistRepo = PersistRepository<Component>(::currentComponent, pipelineDao::insertComponent)

    // ---------------------------------------------------------------------------

    private var currentConfigurationId = ""

    private fun liveConfiguration(configurationId: String = currentConfigurationId) = pipelineDao.liveConfigurationById(configurationId)

    var currentComponent: Component? = null
        set(value) {
            field = value
            value?.let {
                currentComponentId = it.id
                currentConfigurationId = it.configurationId
            }
        }

    private val configStorage = object {
        var lastComponentId = ""
        var statusConfigInput: StatusWithConfigInput? = null
        var statusDialogJs: StatusWithDialogJs? = null
        //var configuration: Configuration? = null
        val configurationMap = mutableMapOf<String, Configuration>()
        fun reset(id: String) {
            lastComponentId = id
            statusConfigInput = null
            statusDialogJs = null
            //configuration = null
        }
    }

    private fun getConfigInputMediator() = MediatorLiveData<ConfigInputContext>().also { mediator ->

        fun updateConfigInputMediator() {
            configStorage.statusDialogJs?.let {
                if (configStorage.statusConfigInput == null) {
                    mediator.value = OnlyStatus(it.status.result.toStatus)
                }
            }
            val sConfigInput = configStorage.statusConfigInput ?: return
            val sConfigInputStatus = sConfigInput.status.result.toStatus
            if (sConfigInputStatus != StatusCode.OK) {
                mediator.value = OnlyStatus(sConfigInputStatus)
                return
            }
            val sDialog = configStorage.statusDialogJs ?: return
            val sDialogStatus = sDialog.status.result.toStatus
            if (sDialogStatus != StatusCode.OK) {
                mediator.value = OnlyStatus(sDialogStatus)
                return
            }
            if (sDialog.dialogJs == null) {
                mediator.value = OnlyStatus(StatusCode.INTERNAL_ERROR)
                return
            }
            //val configuration = configStorage.configuration ?: return
            mediator.value = ConfigInputComplete(
                StatusCode.OK,
                //configuration,
                sDialog.dialogJs,
                sConfigInput.list
            )
        }

        updateConfigInputMediator()

        mediator.addSource(liveConfigInput()) {
            if (configStorage.statusConfigInput?.status?.result.toStatus == StatusCode.OK)
                return@addSource
            configStorage.statusConfigInput = it ?: return@addSource
            updateConfigInputMediator()
        }
        mediator.addSource(liveDialogJs()) {
            if (configStorage.statusDialogJs?.status?.result.toStatus == StatusCode.OK)
                return@addSource
            configStorage.statusDialogJs = it ?: return@addSource
            updateConfigInputMediator()
        }
        mediator.addSource(liveConfiguration()) {
            /*if (configStorage.configuration != null)
                return@addSource
            configStorage.configuration = it ?: return@addSource*/
            if (configStorage.configurationMap[currentComponentId] != null)
                return@addSource
            configStorage.configurationMap[currentComponentId] = it ?: return@addSource
            updateConfigInputMediator()
        }
    }

    //private val updateConfigurationMutex = Mutex()
    suspend fun updateConfiguration(componentId: String) /*= updateConfigurationMutex.withLock*/ {
        val configuration = configStorage.configurationMap[componentId] ?: return//@withLock
        pipelineDao.insertConfiguration(configuration)
    }

    val liveConfigInputContext: LiveData<ConfigInputContext>
        get() = synchronized(this) {
            if (currentComponentId != configStorage.lastComponentId) {
                configStorage.reset(currentComponentId)
            }
            return@synchronized getConfigInputMediator()
        }

    fun configGetString(key: String) = configStorage.configurationMap[currentComponentId]?.getString(key)
    fun configSetString(key: String, value: String) = configStorage.configurationMap[currentComponentId]?.setString(key, value)

    // ---------------------------------------------------------------------------

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
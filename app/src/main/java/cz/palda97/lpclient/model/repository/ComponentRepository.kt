package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ComponentRetrofit
import cz.palda97.lpclient.model.network.ComponentRetrofit.Companion.componentRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import kotlinx.coroutines.*

class ComponentRepository(
    private val serverDao: ServerInstanceDao
) {

    /*private val currentServerId: Long?
        get() = Injector.pipelineRepository.currentPipeline.value?.mailContent?.pipelineView?.serverId*/

    private var currentServerId: Long? = null

    enum class StatusCode {
        NO_CONNECT, INTERNAL_ERROR, SERVER_NOT_FOUND, DOWNLOADING_ERROR, PARSING_ERROR, OK
    }

    private suspend fun getComponentRetrofit(server: ServerInstance): Either<StatusCode, ComponentRetrofit> =
        try {
            Either.Right(RetrofitHelper.getBuilder(server, server.frontendUrl).componentRetrofit)
        } catch (e: IllegalArgumentException) {
            l("getComponentRetrofit ${e.toString()}")
            Either.Left(StatusCode.NO_CONNECT)
        }

    private suspend fun getComponentRetrofit(component: Component): Either<StatusCode, ComponentRetrofit> {
        val id = currentServerId ?: return Either.Left(StatusCode.INTERNAL_ERROR)
        val server = serverDao.findById(id) ?: return Either.Left(StatusCode.SERVER_NOT_FOUND)
        return getComponentRetrofit(server)
    }

    /*private val currentPipeline: Pipeline?
        get() = Injector.pipelineRepository.currentPipeline.value?.mailContent.also {
            if (it == null)
                l("currentPipeline is null")
        }*/
    private var currentPipeline: Pipeline? = null
        get() = field.also {
            if (it == null)
                l("currentPipeline is null")
        }

    private suspend fun downloadDialog(component: Component): Either<StatusCode, List<ConfigInput>> {
        val retrofit = when (val res = getComponentRetrofit(component)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val templateId = component.getRootTemplate(currentPipeline) ?: return Either.Left(StatusCode.INTERNAL_ERROR)
        val call = retrofit.dialog(templateId)
        val text = RetrofitHelper.getStringFromCall(call)
            ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = ConfigInputFactory(text)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private val configInputMap: MutableMap<String, List<ConfigInput>> = HashMap()

    private suspend fun cacheConfigInput(component: Component): StatusCode {
        if (configInputMap.contains(component.id)) {
            return StatusCode.OK
        }
        val list = when (val res = downloadDialog(component)) {
            is Either.Left -> return res.value
            is Either.Right -> res.value
        }
        configInputMap[component.id] = list
        return StatusCode.OK
    }

    suspend fun cache(components: List<Component>, server: Long, pipeline: Pipeline) {
        currentServerId = server
        currentPipeline = pipeline
        cacheConfigInput(components)
        cacheJsMap(components)
        cacheBindings(components)
    }

    private suspend fun cacheConfigInput(components: List<Component>) = coroutineScope {
        configInputMap.clear()
        val jobs = components.map {
            async {
                it.id to cacheConfigInput(it)
            }
        }
        jobs.forEach {
            val (componentId, status) = it.await()
            if (status != StatusCode.OK) {
                val template = Injector.pipelineRepository.currentPipeline.value?.mailContent?.components?.find { it.id == componentId }?.templateId
                l("cacheConfigInput $status: $componentId\n$template")
            }
        }
    }

    var currentComponentId: String? = null

    private val mutConfigInput: MutableLiveData<MailPackage<Either<StatusCode, List<ConfigInput>>>> =
        MutableLiveData()
    val liveConfigInput: LiveData<MailPackage<Either<StatusCode, List<ConfigInput>>>>
        get() = mutConfigInput

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    fun prepare(component: Component, pipeline: Pipeline) {
        currentPipeline = pipeline
        prepareConfigInput(component)
        prepareJsMap(component)
        prepareBindings(component)
    }

    private fun prepareConfigInput(component: Component) {
        mutConfigInput.value = MailPackage.loadingPackage()
        retrofitScope.launch {
            val status = cacheConfigInput(component)
            val either: Either<StatusCode, List<ConfigInput>> = if (status == StatusCode.OK) {
                configInputMap[component.id]?.let {
                    Either.Right<StatusCode, List<ConfigInput>>(it)
                } ?: Either.Left(StatusCode.INTERNAL_ERROR)
            } else {
                Either.Left(status)
            }
            mutConfigInput.postValue(MailPackage(either))
        }
    }

    private suspend fun downloadDialogJs(component: Component): Either<StatusCode, DialogJs> {
        val retrofit = when (val res = getComponentRetrofit(component)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val templateId = component.getRootTemplate(currentPipeline) ?: return Either.Left(StatusCode.INTERNAL_ERROR)
        val call = retrofit.dialogJs(templateId)
        val text = RetrofitHelper.getStringFromCall(call)
            ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = DialogJsFactory(text)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private val jsMapMap: MutableMap<String, DialogJs> = HashMap()

    private suspend fun cacheJsMap(component: Component): StatusCode {
        if (jsMapMap.contains(component.id)) {
            return StatusCode.OK
        }
        val jsMap = when (val res = downloadDialogJs(component)) {
            is Either.Left -> return res.value
            is Either.Right -> res.value
        }
        jsMapMap[component.id] = jsMap
        return StatusCode.OK
    }

    private suspend fun cacheJsMap(components: List<Component>) = coroutineScope {
        jsMapMap.clear()
        val jobs = components.map {
            async {
                it.id to cacheJsMap(it)
            }
        }
        jobs.forEach {
            val (componentId, status) = it.await()
            if (status != StatusCode.OK) {
                val template = Injector.pipelineRepository.currentPipeline.value?.mailContent?.components?.find { it.id == componentId }?.templateId
                l("cacheJsMap $status: $componentId\n$template")
            }
        }
    }

    private val mutJsMap: MutableLiveData<MailPackage<Either<StatusCode, DialogJs>>> =
        MutableLiveData()
    val liveJsMap: LiveData<MailPackage<Either<StatusCode, DialogJs>>>
        get() = mutJsMap

    private fun prepareJsMap(component: Component) {
        mutJsMap.value = MailPackage.loadingPackage()
        retrofitScope.launch {
            val status = cacheJsMap(component)
            val either: Either<StatusCode, DialogJs> = if (status == StatusCode.OK) {
                jsMapMap[component.id]?.let {
                    Either.Right<StatusCode, DialogJs>(it)
                } ?: Either.Left(StatusCode.INTERNAL_ERROR)
            } else {
                Either.Left(status)
            }
            mutJsMap.postValue(MailPackage(either))
        }
    }

    private fun Component.getRootTemplate(pipeline: Pipeline?): String? {
        if (pipeline == null)
            return null
        tailrec fun Component.rec(pipeline: Pipeline): String {
            val template = pipeline.templates.find {
                it.id == templateId
            } ?: return templateId
            return Component(0, 0, template).rec(pipeline)
        }
        return rec(pipeline)
    }

    private suspend fun downloadBindings(component: Component): Either<StatusCode, List<Binding>> {
        val retrofit = when (val res = getComponentRetrofit(component)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val templateId = component.getRootTemplate(currentPipeline) ?: return Either.Left(StatusCode.INTERNAL_ERROR)
        val call = retrofit.bindings(templateId)
        val text = RetrofitHelper.getStringFromCall(call)
            ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = BindingFactory(text)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private val bindingMap: MutableMap<String, List<Binding>> = HashMap()

    private suspend fun cacheBindings(component: Component): StatusCode {
        if (bindingMap.contains(component.id)) {
            return StatusCode.OK
        }
        val list = when (val res = downloadBindings(component)) {
            is Either.Left -> return res.value
            is Either.Right -> res.value
        }
        bindingMap[component.id] = list
        return StatusCode.OK
    }

    private suspend fun cacheBindings(components: List<Component>) = coroutineScope {
        bindingMap.clear()
        val jobs = components.map {
            async {
                it.id to cacheBindings(it)
            }
        }
        jobs.forEach {
            val (componentId, status) = it.await()
            if (status != StatusCode.OK) {
                val template = Injector.pipelineRepository.currentPipeline.value?.mailContent?.components?.find { it.id == componentId }?.templateId
                l("cacheBindings $status: $componentId\n$template")
            }
        }
    }

    private val mutBindings: MutableLiveData<MailPackage<Either<StatusCode, List<Binding>>>> =
        MutableLiveData()
    val liveBindings: LiveData<MailPackage<Either<StatusCode, List<Binding>>>>
        get() = mutBindings

    private fun prepareBindings(component: Component) {
        mutBindings.value = MailPackage.loadingPackage()
        retrofitScope.launch {
            val status = cacheBindings(component)
            val either: Either<StatusCode, List<Binding>> = if (status == StatusCode.OK) {
                bindingMap[component.id]?.let {
                    Either.Right<StatusCode, List<Binding>>(it)
                } ?: Either.Left(StatusCode.INTERNAL_ERROR)
            } else {
                Either.Left(status)
            }
            mutBindings.postValue(MailPackage(either))
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
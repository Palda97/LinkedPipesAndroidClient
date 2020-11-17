package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.entities.pipeline.ConfigInputFactory
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

    private suspend fun downloadDialog(component: Component): Either<StatusCode, List<ConfigInput>> {
        val retrofit = when (val res = getComponentRetrofit(component)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val call = retrofit.dialog(component.templateId)
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

    suspend fun cacheConfigInput(components: List<Component>, server: Long) = coroutineScope {
        currentServerId = server
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

    fun prepareConfigInput(component: Component) {
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

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.entities.pipeline.ConfigInputFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ComponentRetrofit
import cz.palda97.lpclient.model.network.ComponentRetrofit.Companion.componentRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper

class ComponentRepository(
    private val serverDao: ServerInstanceDao
) {

    private val _liveComponent: MutableLiveData<Component> = MutableLiveData()
    val liveComponent: LiveData<Component>
        get() = _liveComponent

    var currentComponent: Component? = null
        set(value) {
            _liveComponent.value = value
            field = value
        }

    private val currentServerId: Long?
        get() = Injector.pipelineRepository.currentPipeline.value?.mailContent?.pipelineView?.serverId

    enum class StatusCode {
        NO_CONNECT, OK, INTERNAL_ERROR, SERVER_NOT_FOUND, DOWNLOADING_ERROR, PARSING_ERROR
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

    suspend fun downloadDialog(component: Component): Either<StatusCode, List<ConfigInput>> {
        val retrofit = when(val res = getComponentRetrofit(component)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val call = retrofit.dialog(component.templateId)
        val text = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = ConfigInputFactory(text)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
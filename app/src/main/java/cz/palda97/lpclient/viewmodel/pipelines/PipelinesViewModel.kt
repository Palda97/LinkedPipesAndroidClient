package cz.palda97.lpclient.viewmodel.pipelines

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.google.gson.Gson
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.entities.pipeline.PipelineView
import cz.palda97.lpclient.model.entities.pipeline.ServerWithPipelineViews
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.model.repository.ServerRepository
import cz.palda97.lpclient.model.services.ExecutionMonitor
import cz.palda97.lpclient.viewmodel.executions.ExecutionV
import kotlinx.coroutines.*

class PipelinesViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val executionRepository: ExecutionRepository = Injector.executionRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)
    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val livePipelineViews: LiveData<MailPackage<List<PipelineView>>> =
        pipelineRepository.liveServersWithPipelineViews.switchMap {
            l("switchMap")
            liveData(Dispatchers.Default) {
                val mail = pipelineViewTransform(it)
                emit(mail)
                l("switchMap end")
            }
        }

    private suspend fun pipelineViewTransform(it: MailPackage<List<ServerWithPipelineViews>>?): MailPackage<List<PipelineView>> =
        withContext(Dispatchers.Default) {
            l("pipelineViewTransform thread: ${Thread.currentThread().name}")
            val mail = it ?: return@withContext MailPackage.loadingPackage<List<PipelineView>>()
            if (mail.isOk) {
                mail.mailContent!!
                mail.mailContent.forEach {
                    l("pipelineViewTransform - ${it.server.id} - ${it.server.name}")
                }
                val list = mail.mailContent.flatMap {
                    it.pipelineViewList.filter { !(it.mark != null || pipelineRepository.deleteRepo.toBeDeleted(it.pipelineView)) }.map { it.pipelineView }.apply {
                        forEach { pipelineView ->
                            pipelineView.serverName = it.server.name
                        }
                    }.sortedByDescending {
                        it.id
                    }
                }
                l("pipelineViewTransform before ok return")
                return@withContext MailPackage(list)
            }
            if (mail.isError)
                return@withContext MailPackage.brokenPackage<List<PipelineView>>(mail.msg)
            l("still loading")
            return@withContext MailPackage.loadingPackage<List<PipelineView>>()
        }

    private suspend fun downloadAllPipelineViews() {
        //pipelineRepository.downAndCachePipelineViews(serverRepository.activeLiveServers.value?.mailContent)
        pipelineRepository.refreshPipelineViews(Either.Right(serverRepository.activeLiveServers.value?.mailContent))
    }

    fun refreshButton() {
        retrofitScope.launch {
            //downloadAllPipelineViews()
            RepositoryRoutines().refresh()
        }
    }

    private fun onServerToFilterChange() {
        //pipelineRepository.onServerToFilterChange()
        RepositoryRoutines().onServerToFilterChange()
    }

    var serverToFilter: ServerInstance?
        get() = serverRepository.serverToFilter
        private set(value) {
            val changed = value != serverRepository.serverToFilter
            serverRepository.serverToFilter = value
            if (changed) {
                onServerToFilterChange()
            }
        }

    fun setServerToFilterFun(serverInstance: ServerInstance?) {
        serverToFilter = serverInstance
    }

    private suspend fun deletePipelineRoutine(pipelineView: PipelineView) {
        pipelineRepository.markForDeletion(pipelineView)
        pipelineRepository.deleteRepo.addPending(pipelineView, DELETE_DELAY)
    }

    fun deletePipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            deletePipelineRoutine(pipelineView)
        }
    }

    private suspend fun cancelRoutine(pipelineView: PipelineView) {
        pipelineRepository.unMarkForDeletion(pipelineView)
        pipelineRepository.deleteRepo.cancelDeletion(pipelineView)
    }

    fun cancelDeletion(pipelineView: PipelineView) {
        dbScope.launch {
            cancelRoutine(pipelineView)
        }
    }

    enum class LaunchStatus {
        PIPELINE_NOT_FOUND, SERVER_NOT_FOUND, CAN_NOT_CONNECT, INTERNAL_ERROR, SERVER_ERROR, WAITING, OK, PROTOCOL_PROBLEM
    }

    private val _launchStatus: MutableLiveData<LaunchStatus> = MutableLiveData(LaunchStatus.WAITING)
    val launchStatus: LiveData<LaunchStatus>
        get() = _launchStatus

    fun resetLaunchStatus() {
        _launchStatus.value = LaunchStatus.WAITING
    }

    private fun launchStatusCodeToLiveData(statusCode: PipelineRepository.StatusCode) {
        _launchStatus.postValue(
            when (statusCode) {
                PipelineRepository.StatusCode.SERVER_ID_NOT_FOUND -> LaunchStatus.SERVER_NOT_FOUND
                PipelineRepository.StatusCode.NO_CONNECT -> LaunchStatus.PROTOCOL_PROBLEM
                PipelineRepository.StatusCode.NULL_RESPONSE -> LaunchStatus.PIPELINE_NOT_FOUND
                PipelineRepository.StatusCode.INTERNAL_ERROR -> LaunchStatus.CAN_NOT_CONNECT
                else -> LaunchStatus.INTERNAL_ERROR
            }
        )
    }

    private suspend fun launchPipelineRoutine(pipelineView: PipelineView) {
        val pipelineString =
            when (val res = pipelineRepository.downloadPipelineString(pipelineView)) {
                is Either.Left -> {
                    launchStatusCodeToLiveData(res.value)
                    return
                }
                is Either.Right -> res.value
            }
        val pipelineRetrofit =
            when (val res = pipelineRepository.getPipelineRetrofit(pipelineView)) {
                is Either.Left -> return
                is Either.Right -> res.value
            }
        val call = pipelineRetrofit.executePipeline(RetrofitHelper.stringToFormData(pipelineString))
        val text = RetrofitHelper.getStringFromCall(call)
        if (text == null) {
            _launchStatus.postValue(LaunchStatus.SERVER_ERROR)
            return
        }
        l(text)
        _launchStatus.postValue(LaunchStatus.OK)
        val iri = Gson().fromJson(text, Iri::class.java)?.let { iri ->
            l("mam iri")
            serverRepository.activeLiveServers.value?.mailContent?.find {
                l("mam server")
                it.id == pipelineView.serverId
            }?.let {
                //executionRepository.monitor(it.id, iri.iri)
                ExecutionMonitor.enqueue(getApplication(), iri.iri, it.id)
            }
        }
    }

    fun launchPipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            launchPipelineRoutine(pipelineView)
            val server = serverRepository.activeLiveServers.value?.mailContent?.find {
                it.id == pipelineView.serverId
            }
            server?.let {
                executionRepository.cacheExecutions(Either.Left(it), true)
            }
        }
    }

    fun launchPipeline(executionV: ExecutionV) {
        retrofitScope.launch {
            val pipelineView = executionRepository.find(executionV.id)?.let {
                pipelineRepository.findPipelineViewById(it.pipelineId)
            }
            if (pipelineView == null) {
                _launchStatus.postValue(LaunchStatus.INTERNAL_ERROR)
                return@launch
            }
            launchPipelineRoutine(pipelineView)
        }
    }

    class Iri(val iri: String)

    companion object {
        private const val TAG = "PipelinesViewModel"
        private fun l(msg: String) = Log.d(TAG, msg)

        private const val DELETE_DELAY: Long = 5000L
    }
}
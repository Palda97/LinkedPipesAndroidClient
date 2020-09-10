package cz.palda97.lpclient.viewmodel.pipelines

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.*

class PipelinesViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)
    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val livePipelineViews: LiveData<MailPackage<List<PipelineView>>> =
        pipelineRepository.liveServersWithPipelineViews.switchMap {
            l("switchMap")
            liveData(Dispatchers.Default) {
                emit(MailPackage.loadingPackage())
                //delay(2000)
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
                val list = mutableListOf<PipelineView>()
                list.addAll(mail.mailContent.flatMap {
                    it.pipelineViewList.filter { !it.deleted }.apply {
                        forEach { pipelineView ->
                            pipelineView.serverName = it.server.name
                        }
                    }.sortedByDescending {
                        it.id
                    }
                })
                l("pipelineViewTransform before ok return")
                return@withContext MailPackage(list.toList())
            }
            if (mail.isError)
                return@withContext MailPackage.brokenPackage<List<PipelineView>>(mail.msg)
            return@withContext MailPackage.loadingPackage<List<PipelineView>>()
        }

    private suspend fun downloadAllPipelineViews() {
        //pipelineRepository.downAndCachePipelineViews(serverRepository.activeLiveServers.value?.mailContent)
        pipelineRepository.refreshPipelineViews(Either.Right(serverRepository.activeLiveServers.value?.mailContent))
    }

    fun refreshButton() {
        retrofitScope.launch {
            downloadAllPipelineViews()
        }
    }

    private fun onServerToFilterChange() {
        pipelineRepository.onServerToFilterChange()
    }

    var serverToFilter: ServerInstance?
        get() = serverRepository.serverToFilter
        set(value) {
            val changed = value != serverRepository.serverToFilter
            serverRepository.serverToFilter = value
            if (changed) {
                onServerToFilterChange()
            }
        }

    private suspend fun deletePipelineRoutine(pipelineView: PipelineView) {
        pipelineRepository.insertPipelineView(pipelineView.apply { deleted = true })
        l("${pipelineView.prefLabel} marked for deletion")
        delay(DELETE_DELAY)
        val pipe = pipelineRepository.findPipelineViewById(pipelineView.id) ?: return
        if (pipe.deleted) {
            pipelineRepository.deletePipeline(pipelineView)
        }
    }

    fun deletePipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            deletePipelineRoutine(pipelineView)
        }
    }

    fun cancelDeletion(pipelineView: PipelineView) {
        dbScope.launch {
            pipelineRepository.insertPipelineView(pipelineView.apply { deleted = false })
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
    }

    fun launchPipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            launchPipelineRoutine(pipelineView)
        }
    }

    companion object {
        private const val TAG = "PipelinesViewModel"
        private fun l(msg: String) = Log.d(TAG, msg)

        private const val DELETE_DELAY: Long = 5000L
    }
}
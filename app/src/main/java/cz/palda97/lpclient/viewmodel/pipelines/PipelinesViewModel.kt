package cz.palda97.lpclient.viewmodel.pipelines

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
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
                val list = mutableListOf<PipelineView>()
                list.addAll(mail.mailContent.flatMap {
                    it.pipelineViewList.filter { !it.deleted }.apply {
                        forEach { pipelineView ->
                            pipelineView.serverName = it.server.name
                        }
                    }
                }.sortedBy {
                    it.id
                })
                l("pipelineViewTransform before ok return")
                return@withContext MailPackage(list.toList())
            }
            if (mail.isError)
                return@withContext MailPackage.brokenPackage<List<PipelineView>>(mail.msg)
            return@withContext MailPackage.loadingPackage<List<PipelineView>>()
        }

    /*private suspend fun downloadPipelineViews() {
        val serverToFilter = serverRepository.serverToFilter
        if (serverToFilter == null)
            pipelineRepository.downAndCachePipelineViews(serverRepository.activeLiveServers.value?.mailContent)
        else
            pipelineRepository.downAndCachePipelineViews(serverToFilter)
    }*/

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

    enum class DeleteStatus {
        SERVER_ID_NOT_FOUND, NO_CONNECT, PIPELINE_NOT_FOUND, OK, INTERNAL_ERROR, WAITING, DELAY
    }

    //private val _

    private suspend fun deletePipelineRoutine(pipelineView: PipelineView) {
        pipelineRepository.insertPipelineView(pipelineView.apply { deleted = true })
        l("${pipelineView.prefLabel} marked for deletion")
        //TODO(somehow let the view know)
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

    companion object {
        private const val TAG = "PipelinesViewModel"
        private fun l(msg: String) = Log.d(TAG, msg)

        private const val DELETE_DELAY: Long = 5000L
    }
}
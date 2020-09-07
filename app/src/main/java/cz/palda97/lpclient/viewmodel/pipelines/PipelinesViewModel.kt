package cz.palda97.lpclient.viewmodel.pipelines

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.PipelineView
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.ServerWithPipelineViews
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.*

class PipelinesViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    //private val _livePipelineViews: MutableLiveData<MailPackage<List<PipelineView>>> = MutableLiveData()
    /*val livePipelineViews: LiveData<MailPackage<List<ServerWithPipelineViews>>>
        get() = pipelineRepository.livePipelineViews*/
    /*val livePipelineViews: LiveData<MailPackage<List<PipelineView>>> =
        Transformations.map(pipelineRepository.livePipelineViews) {
            val mail = it ?: return@map MailPackage.loadingPackage<List<PipelineView>>()
            if (mail.isOk) {
                mail.mailContent!!
                val list = mutableListOf<PipelineView>()
                list.addAll(mail.mailContent.flatMap {
                    it.pipelineViewList.apply {
                        forEach { pipelineView ->
                            pipelineView.serverName = it.server.name
                        }
                    }
                })
                return@map MailPackage(list.toList())
            }
            if (mail.isError)
                return@map MailPackage.brokenPackage<List<PipelineView>>(mail.msg)

            return@map MailPackage.loadingPackage<List<PipelineView>>()
        }*/
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
                    it.pipelineViewList.apply {
                        forEach { pipelineView ->
                            pipelineView.serverName = it.server.name
                        }
                    }
                })
                l("pipelineViewTransform before ok return")
                return@withContext MailPackage(list.toList())
            }
            if (mail.isError)
                return@withContext MailPackage.brokenPackage<List<PipelineView>>(mail.msg)
            return@withContext MailPackage.loadingPackage<List<PipelineView>>()
        }

    /*fun refreshPipelines() {
        val serverToFilter = serverRepository.serverToFilter
        retrofitScope.launch {
            val pipelines = if (serverToFilter == null)
                pipelineRepository.downloadPipelineViews(serverRepository.liveServers.value?.mailContent)
            else
                pipelineRepository.downloadPipelineViews(serverToFilter)
            _livePipelineViews.postValue(pipelines)
        }
    }*/

    fun refreshPipelines() {
        val serverToFilter = serverRepository.serverToFilter
        retrofitScope.launch {
            if (serverToFilter == null)
                pipelineRepository.downAndCachePipelineViews(serverRepository.liveServers.value?.mailContent)
            else
                pipelineRepository.downAndCachePipelineViews(serverToFilter)
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

    fun deletePipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            pipelineRepository.deletePipeline(pipelineView)
        }
    }

    companion object {
        private const val TAG = "PipelinesViewModel"
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}
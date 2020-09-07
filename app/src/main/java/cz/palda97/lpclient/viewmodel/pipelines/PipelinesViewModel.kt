package cz.palda97.lpclient.viewmodel.pipelines

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
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
    val livePipelineViews: LiveData<MailPackage<List<PipelineView>>> = pipelineRepository.livePipelineViews.switchMap {
        liveData(Dispatchers.Default) {
            emit(MailPackage.loadingPackage())
            val mail = pipelineViewTransform(it)
            emit(mail)
        }
    }
    private fun pipelineViewTransform(it: MailPackage<List<ServerWithPipelineViews>>?): MailPackage<List<PipelineView>> {
        //withContext(Dispatchers.Default) {
            val tname = Thread.currentThread().name
            l("pipelineViewTransform thread: $tname")
            val mail = it ?: return MailPackage.loadingPackage<List<PipelineView>>()
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
                return MailPackage(list.toList())
            }
            if (mail.isError)
                return MailPackage.brokenPackage<List<PipelineView>>(mail.msg)
            return MailPackage.loadingPackage<List<PipelineView>>()
        //}
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
            val pipelines = if (serverToFilter == null)
                pipelineRepository.downAndCachePipelineViews(serverRepository.liveServers.value?.mailContent)
            else
                pipelineRepository.downAndCachePipelineViews(serverToFilter)
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
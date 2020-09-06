package cz.palda97.lpclient.viewmodel.pipelines

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PipelinesViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    fun idk() {
        retrofitScope.launch {
            pipelineRepository.idk()
        }
    }

    private val _livePipelineViews: MutableLiveData<MailPackage<List<PipelineView>>> = MutableLiveData()
    val livePipelineViews: LiveData<MailPackage<List<PipelineView>>>
        get() = _livePipelineViews

    fun refreshPipelines() {
        val serverToFilter = serverRepository.serverToFilter
        retrofitScope.launch {
            val pipelines = if (serverToFilter == null)
                pipelineRepository.downloadPipelineViews(serverRepository.liveServers.value?.mailContent)
            else
                pipelineRepository.downloadPipelineViews(serverToFilter)
            _livePipelineViews.postValue(pipelines)
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
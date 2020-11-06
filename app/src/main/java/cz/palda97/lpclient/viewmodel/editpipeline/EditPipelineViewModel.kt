package cz.palda97.lpclient.viewmodel.editpipeline

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.repository.PipelineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditPipelineViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val currentPipeline: LiveData<MailPackage<Pipeline>>
        get() = pipelineRepository.currentPipeline

    fun savePipeline(pipeline: Pipeline) {
        pipelineRepository.savePipeline(pipeline)
    }

    fun cachePipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            pipelineRepository.cachePipeline(pipelineView)
        }
    }

    fun retryCachePipeline() {
        retrofitScope.launch {
            pipelineRepository.retryCachePipeline()
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditPipelineViewModel::class.java)
    }
}
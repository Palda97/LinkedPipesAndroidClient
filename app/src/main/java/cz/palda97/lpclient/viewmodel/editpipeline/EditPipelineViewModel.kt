package cz.palda97.lpclient.viewmodel.editpipeline

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.PipelineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditPipelineViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val componentRepository: ComponentRepository = Injector.componentRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val currentPipeline: LiveData<MailPackage<Pipeline>>
        get() = pipelineRepository.currentPipeline

    fun savePipeline(pipeline: Pipeline) {
        pipelineRepository.savePipeline(pipeline)
    }

    fun retryCachePipeline() {
        retrofitScope.launch {
            pipelineRepository.retryCachePipeline()
        }
    }

    var shouldScroll: Boolean
        get() {
            val res = scroll
            scroll = false
            return res
        }
        set(value) {
            scroll = value
        }

    fun editComponent(component: Component) {
        componentRepository.currentComponent = component
        scroll = true
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditPipelineViewModel::class.java)

        var scroll = false
    }
}
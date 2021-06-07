package cz.palda97.lpclient.viewmodel.editpipeline

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.Template
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.PossibleComponentRepository
import kotlinx.coroutines.*

/**
 * ViewModel for the [EditPipelineActivity][cz.palda97.lpclient.view.EditPipelineActivity].
 */
class EditPipelineViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val componentRepository: ComponentRepository = Injector.componentRepository
    private val possibleRepository: PossibleComponentRepository = Injector.possibleComponentRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    /** @see PipelineRepository.livePipeline */
    val currentPipeline: LiveData<MailPackage<Pipeline>>
        get() = pipelineRepository.livePipeline

    /**
     * Replace the pipeline in database.
     * @param pipeline Pipeline to be saved.
     */
    fun savePipeline(pipeline: Pipeline) {
        retrofitScope.launch {
            pipelineRepository.savePipeline(pipeline, false)
        }
    }

    /** @see PipelineRepository.retryCachePipeline */
    fun retryCachePipeline() {
        retrofitScope.launch {
            pipelineRepository.retryCachePipeline()
        }
    }

    /** @see scroll */
    var shouldScroll: Boolean
        get() {
            val res = scroll
            scroll = false
            return res
        }
        set(value) {
            scroll = value
        }

    /**
     * Prepare [ComponentRepository] for editing of this component.
     */
    fun editComponent(component: Component, templates: List<Template>) {
        componentRepository.setImportantIds(component, templates)
        retrofitScope.launch {
            componentRepository.cache(component)
        }
    }

    /** @see PossibleComponentRepository.prepareForNewComponent */
    fun addComponent(coords: Pair<Int, Int>) {
        possibleRepository.prepareForNewComponent(coords)
    }

    /** @see PossibleComponentRepository.mutableLiveAddComponentStatus */
    val liveAddComponentStatus: LiveData<PossibleComponentRepository.StatusCode>
        get() = possibleRepository.mutableLiveAddComponentStatus

    /**
     * Sets the [mutableLiveAddComponentStatus][PossibleComponentRepository.mutableLiveAddComponentStatus]
     * to [OK][PossibleComponentRepository.StatusCode.OK].
     */
    fun resetAddComponentStatus() {
        possibleRepository.mutableLiveAddComponentStatus.value = PossibleComponentRepository.StatusCode.OK
    }

    /** @see PipelineRepository.currentPipelineView */
    var currentPipelineView
        get() = pipelineRepository.currentPipelineView!!
        set(value) {
            pipelineRepository.currentPipelineView = value
        }

    /**
     * Tries to save the [Pipeline] to the database.
     * @return [Job][kotlinx.coroutines.Job] related to saving the pipeline
     * or null if the argument is null.
     */
    suspend fun uploadPipelineButton(pipeline: Pipeline?): Boolean = if (pipeline == null) {
        withContext(Dispatchers.Main) {
            pipelineRepository.cannotSavePipelineForUpload()
        }
        false
    } else {
        withContext(Dispatchers.IO) {
            pipelineRepository.savePipeline(pipeline, false)
            currentPipelineView = pipeline.pipelineView
        }
        true
    }

    /** @see PipelineRepository.liveUploadStatus */
    val liveUploadStatus
        get() = pipelineRepository.liveUploadStatus
    fun resetUploadStatus() {
        pipelineRepository.resetUploadStatus()
    }

    /**
     * Uploads [Pipeline] that is currently in database.
     * @return [Job][kotlinx.coroutines.Job] related to this process.
     */
    fun uploadPipeline() = retrofitScope.launch {
        pipelineRepository.insertCurrentPipelineView()
        pipelineRepository.uploadPipeline()
    }

    /** @see [PipelineRepository.pipelineLink] */
    suspend fun pipelineLink() = withContext(Dispatchers.IO) {
        pipelineRepository.pipelineLink()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Gets an instance of [EditPipelineViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditPipelineViewModel::class.java)

        /**
         * If the app should scroll closer to the components or not.
         */
        var scroll = false
    }
}
package cz.palda97.lpclient.viewmodel.editpipeline

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.IdGenerator
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.PossibleComponentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the [AddComponentDialog][cz.palda97.lpclient.view.editpipeline.AddComponentDialog].
 */
class AddComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val possibleRepository: PossibleComponentRepository = Injector.possibleComponentRepository
    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val componentRepository: ComponentRepository = Injector.componentRepository

    private val retrofitScope
        get() = CoroutineScope(Dispatchers.IO)

    /**
     * Get configuration and templates needed for the creation
     * of a [Component] based on the given [PossibleComponent].
     * @return [Job][kotlinx.coroutines.Job] related to this process.
     */
    fun addComponent(possibleComponent: PossibleComponent) = retrofitScope.launch {
        val id = IdGenerator.componentId(pipelineRepository.currentPipelineId)
        val configuration = when(val res = possibleRepository.downloadDefaultConfiguration(possibleComponent, id)) {
            is Either.Left -> {
                possibleRepository.mutableLiveAddComponentStatus.postValue(res.value)
                return@launch
            }
            is Either.Right -> res.value
        }
        val coords = possibleRepository.coords
        if (coords == null) {
            possibleRepository.mutableLiveAddComponentStatus.postValue(PossibleComponentRepository.StatusCode.INTERNAL_ERROR)
            return@launch
        }
        l("coords = $coords")
        val component = Component(
            configuration.id,
            possibleComponent.id,
            coords.first,
            coords.second,
            possibleComponent.prefLabel,
            null,
            id
        )
        val (templateBranch, rootTemplate) = when(val res = possibleRepository.getTemplatesAndConfigurations(possibleComponent)) {
            is Either.Left -> {
                possibleRepository.mutableLiveAddComponentStatus.postValue(res.value)
                return@launch
            }
            is Either.Right -> res.value
        }
        componentRepository.cacheBinding(rootTemplate)
        val templates = templateBranch.map { it.first }
        val configurations = templateBranch.map { it.second }
        possibleRepository.persistTemplate(templates)
        possibleRepository.persistConfiguration(configurations)
        possibleRepository.persistComponent(component)
        possibleRepository.persistConfiguration(configuration)
    }

    /** @see PossibleComponentRepository.liveComponents */
    val liveComponents
        get() = possibleRepository.liveComponents

    /** @see PossibleComponentRepository.lastSelectedComponentPosition */
    var lastSelectedComponentPosition: Int?
        get() = possibleRepository.lastSelectedComponentPosition
        set(value) {
            possibleRepository.lastSelectedComponentPosition = value
        }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Gets an instance of [AddComponentViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(AddComponentViewModel::class.java)
    }
}
package cz.palda97.lpclient.viewmodel.editpipeline

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.IdGenerator
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.PossibleComponentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val possibleRepository: PossibleComponentRepository = Injector.possibleComponentRepository
    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository

    private val retrofitScope
        get() = CoroutineScope(Dispatchers.IO)

    fun addComponent(possibleComponent: PossibleComponent) = retrofitScope.launch {
        val configuration = when(val res = possibleRepository.downloadDefaultConfiguration(possibleComponent)) {
            is Either.Left -> TODO()
            is Either.Right -> res.value
        }
        val coords = possibleRepository.coords ?: TODO()
        l("coords = $coords")
        val component = Component(
            configuration.id,
            possibleComponent.id,
            coords.first,
            coords.second,
            possibleComponent.prefLabel,
            null,
            IdGenerator.componentId(pipelineRepository.currentPipelineId)
        )
        possibleRepository.persistComponent(component)
        possibleRepository.persistConfiguration(configuration)
    }

    val liveComponents
        get() = possibleRepository.liveComponents

    var lastSelectedComponentPosition: Int?
        get() = possibleRepository.lastSelectedComponentPosition
        set(value) {
            possibleRepository.lastSelectedComponentPosition = value
        }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(AddComponentViewModel::class.java)
    }
}
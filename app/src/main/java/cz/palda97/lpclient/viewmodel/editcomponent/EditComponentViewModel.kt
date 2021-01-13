package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.PipelineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val componentRepository: ComponentRepository = Injector.componentRepository
    //private val pipelineRepository: PipelineRepository = Injector.pipelineRepository

    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val liveComponent
        get() = componentRepository.liveComponent

    // -------------------- configuration ----------------------------------------
    val liveConfigInputContext
        get() = componentRepository.configurationRepository.liveConfigInputContext
    fun configGetString(key: String) = componentRepository.configurationRepository.getString(key)
    fun configSetString(key: String, value: String) = componentRepository.configurationRepository.setString(key, value)
    fun persistConfiguration() {
        val componentId = componentRepository.currentComponent?.id ?: return Unit.also {
            l("component in componentRepository is null")
        }
        dbScope.launch {
            componentRepository.configurationRepository.updateConfiguration(componentId)
        }
    }
    // -------------------- configuration / ------------------------------------

    suspend fun prepareComponent() = componentRepository.componentPersistRepo.prepareEntity()
    fun updateComponent(component: Component) {
        componentRepository.componentPersistRepo.entity = component
    }
    fun getComponent() = componentRepository.componentPersistRepo.entity
    fun persistComponent() = dbScope.launch {
        componentRepository.componentPersistRepo.persistEntity()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}
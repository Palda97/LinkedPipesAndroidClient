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

    /*val liveConfigInput
        get() = componentRepository.liveConfigInput()
    val liveDialogJs
        get() = componentRepository.liveDialogJs()
    val liveBinding
        get() = componentRepository.liveBinding()

    suspend fun prepareConfiguration() = componentRepository.configurationPersistRepo.prepareEntity()
    fun configGetString(key: String) = componentRepository.configurationPersistRepo.entity?.getString(key)
    fun configSetString(key: String, value: String) = componentRepository.configurationPersistRepo.entity?.setString(key, value)
    fun persistConfiguration() = dbScope.launch {
        componentRepository.configurationPersistRepo.persistEntity()
    }*/

    val liveComponent
        get() = componentRepository.liveComponent

    // ---------------------------------------------------------------------------

    val liveConfigInputContext
        get() = componentRepository.liveConfigInputContext

    fun configGetString(key: String) = componentRepository.configGetString(key)
    fun configSetString(key: String, value: String) = componentRepository.configSetString(key, value)

    fun persistConfiguration() {
        val componentId = componentRepository.currentComponent?.id ?: return Unit.also {
            l("component in componentRepository is null")
        }
        dbScope.launch {
            componentRepository.updateConfiguration(componentId)
        }
    }

    // ---------------------------------------------------------------------------

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
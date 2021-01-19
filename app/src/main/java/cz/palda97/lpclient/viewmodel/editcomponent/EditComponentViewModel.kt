package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.pipeline.Connection
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

    // -------------------- configuration ----------------------------------------
    val liveConfigInputContext
        get() = componentRepository.configurationRepository.liveConfigInputContext
    fun configGetString(key: String) = componentRepository.configurationRepository.getString(key)
    fun configSetString(key: String, value: String) = componentRepository.configurationRepository.setString(key, value)
    fun persistConfiguration() {
        val componentId = componentRepository.currentComponentId
        if (componentId.isEmpty()) {
            l("persistConfiguration componentId.isEmpty()")
            return
        }
        dbScope.launch {
            componentRepository.configurationRepository.updateConfiguration(componentId)
        }
    }
    // -------------------- configuration / ------------------------------------

    // -------------------- component ------------------------------------------
    val currentComponent
        get() = componentRepository.currentComponent
    fun persistComponent() {
        val componentId = componentRepository.currentComponentId
        if (componentId.isEmpty()) {
            l("persistComponent componentId.isEmpty()")
            return
        }
        dbScope.launch {
            componentRepository.updateComponent(componentId)
        }
    }
    // -------------------- component / ----------------------------------------

    // -------------------- binding --------------------------------------------
    val liveBinding
        get() = componentRepository.bindingRepository.liveBindings()
    val liveInputConnectionContext
        get() = componentRepository.bindingRepository.liveInputContext
    val liveOutputConnectionContext
        get() = componentRepository.bindingRepository.liveOutputContext
    fun saveConnection(connection: Connection) = dbScope.launch {
        componentRepository.persistConnection(connection)
    }
    fun deleteConnection(connection: Connection) = dbScope.launch {
        componentRepository.deleteConnection(connection)
    }
    // -------------------- binding / ------------------------------------------

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}
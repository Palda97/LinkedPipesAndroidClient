package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.pipeline.Connection
import cz.palda97.lpclient.model.entities.pipeline.Vertex
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.ComponentRepository.Companion.getRootTemplateId
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
    fun saveConnection(connection: Connection) = dbScope.launch {
        componentRepository.persistConnection(connection)
    }

    var lastDeletedConnection: Connection? = null
    var lastDeletedVertexes: List<Vertex>? = null
    fun deleteConnection(connection: Connection) = dbScope.launch {
        lastDeletedConnection = connection
        lastDeletedVertexes = componentRepository.deleteConnection(connection)
    }

    fun undoLastDeleted() = dbScope.launch {
        val connection = lastDeletedConnection ?: return@launch
        val vertexes = lastDeletedVertexes ?: return@launch
        componentRepository.persistConnection(connection)
        componentRepository.persistVertexes(vertexes)
    }

    private data class Labels(val component: String, val distant: String, val own: String)
    private fun parseConnectionItemArguments (configInputContext: BindingComplete, componentId: String, bindingValue: String, ownBindingValue: String): Labels {
        val component = configInputContext.components.find { it.id == componentId }
        val templateId = component?.let {
            it.getRootTemplateId(configInputContext.templates)
        }
        val bindingWithStatus = templateId?.let { id ->
            configInputContext.otherBindings.find {
                it.status.componentId == id
            }
        }
        val binding = bindingWithStatus?.let {
            it.list.find {
                it.bindingValue == bindingValue
            }
        }
        val ownBinding = configInputContext.bindings.find {
            it.bindingValue == ownBindingValue
        }
        return Labels(
            component?.prefLabel ?: "",
            binding?.prefLabel ?: "",
            ownBinding?.prefLabel ?: ""
        )
    }

    val liveInputConnectionV: LiveData<ConfigInputContext>
        get() = componentRepository.bindingRepository.liveInputContext.map {configInputContext ->
            if (configInputContext !is BindingComplete) {
                return@map configInputContext
            }

            val connections = configInputContext.connections.map {connection ->
                val labels = parseConnectionItemArguments(
                    configInputContext,
                    connection.sourceComponentId,
                    connection.sourceBinding,
                    connection.targetBinding
                )

                val componentPrefLabel = labels.component
                val sourceBinding = labels.distant
                val targetBinding = labels.own

                connection to ConnectionV.ConnectionItem(componentPrefLabel, sourceBinding, targetBinding)
            }

            ConnectionV(configInputContext.status, connections)
        }

    val liveOutputConnectionV: LiveData<ConfigInputContext>
        get() = componentRepository.bindingRepository.liveOutputContext.map {configInputContext ->
            if (configInputContext !is BindingComplete) {
                return@map configInputContext
            }

            val connections = configInputContext.connections.map {connection ->
                val labels = parseConnectionItemArguments(
                    configInputContext,
                    connection.targetComponentId,
                    connection.targetBinding,
                    connection.sourceBinding
                )

                val componentPrefLabel = labels.component
                val sourceBinding = labels.own
                val targetBinding = labels.distant

                connection to ConnectionV.ConnectionItem(componentPrefLabel, sourceBinding, targetBinding)
            }

            ConnectionV(configInputContext.status, connections)
        }
    // -------------------- binding / ------------------------------------------

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}
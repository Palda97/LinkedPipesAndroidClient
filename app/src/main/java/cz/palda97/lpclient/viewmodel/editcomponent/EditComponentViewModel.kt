package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.IdGenerator
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.ComponentRepository.Companion.getRootTemplateId
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.travelobjects.LdConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the [EditComponentActivity][cz.palda97.lpclient.view.EditComponentActivity].
 */
class EditComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val componentRepository: ComponentRepository = Injector.componentRepository
    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository

    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    // -------------------- configuration ----------------------------------------
    /**
     * LiveData with content for the [ConfigurationFragment][cz.palda97.lpclient.view.editcomponent.ConfigurationFragment].
     */
    val liveConfigInputContext
        get() = componentRepository.configurationRepository.liveConfigInputContext

    /** @see [Configuration.getString] */
    fun configGetString(key: String, configType: String) = componentRepository.configurationRepository.getString(key, configType)

    /** @see [Configuration.setString] */
    fun configSetString(key: String, value: String, configType: String) = componentRepository.configurationRepository.setString(key, value, configType)

    /**
     * Updates configuration of component that is being edited.
     */
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

    // -------------------- inheritance ------------------------------------------
    /**
     * LiveData with content for the [InheritanceFragment][cz.palda97.lpclient.view.editcomponent.InheritanceFragment].
     */
    val liveInheritances
        get() = liveConfigInputContext.map<ConfigInputContext, Either<ComponentRepository.StatusCode, InheritanceVWrapper>> {
            val cic = when(it) {
                is ConfigInputComplete -> it
                else -> return@map Either.Left(it.status)
            }
            val inheritances = componentRepository.configurationRepository.getInheritances(cic.dialogJs.controlRegex, cic.dialogJs.configType)
                ?: return@map Either.Left(ComponentRepository.StatusCode.PARSING_ERROR)
            val list = inheritances.map {
                val reversedName = cic.dialogJs.fullControlNameToReverse(it.first)
                val configInput = cic.configInputs.find { it.id == reversedName }
                val label = configInput?.label ?: reversedName
                InheritanceV(it.first, label, it.second)
            }
            l(list)
            return@map Either.Right(InheritanceVWrapper(list, cic.dialogJs.configType))
        }

    /** @see [Configuration.setInheritance] */
    fun setInheritance(key: String, value: Boolean, configType: String) = componentRepository.configurationRepository.setInheritance(key, if (value) LdConstants.INHERITANCE_INHERIT else LdConstants.INHERITANCE_NONE, configType)
    // -------------------- inheritance / ----------------------------------------

    // -------------------- component ------------------------------------------
    /**
     * Component that is being edited.
     */
    val currentComponent
        get() = componentRepository.currentComponent

    /**
     * Updates component that is being edited.
     */
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

    /**
     * Deletes component that is being edited.
     */
    fun deleteCurrentComponent() = dbScope.launch {
        componentRepository.deleteCurrentComponent()
    }
    // -------------------- component / ----------------------------------------

    // -------------------- binding --------------------------------------------
    /**
     * LiveData with [bindings with statuses][StatusWithBinding].
     */
    val liveBinding
        get() = componentRepository.bindingRepository.liveBindings()

    /**
     * Last deleted connection stored for the UNDO option.
     */
    var lastDeletedConnection: Connection? = null

    /**
     * Last deleted vertexes stored for the UNDO option.
     */
    var lastDeletedVertexes: List<Vertex>? = null

    /**
     * Deletes connection with it's vertexes from database and store them locally for the UNDO option.
     */
    fun deleteConnection(connection: Connection) = dbScope.launch {
        lastDeletedConnection = connection
        lastDeletedVertexes = componentRepository.deleteConnection(connection)
    }

    /**
     * UNDO method for deleting connection with vertexes.
     */
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

    /**
     * LiveData with display data for input connections.
     */
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
            }.sortedBy {
                it.second.component
            }

            ConnectionV(configInputContext.status, connections)
        }

    /**
     * LiveData with display data for output connections.
     */
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
            }.sortedBy {
                it.second.component
            }

            ConnectionV(configInputContext.status, connections)
        }
    // -------------------- binding / ------------------------------------------

    // -------------------- create connection dialog -----------------------------

    /**
     * Prepare [ConnectionDialogRepository][cz.palda97.lpclient.model.repository.ConnectionDialogRepository]
     * for a new connection.
     */
    fun addConnectionButton(binding: Binding) {
        componentRepository.connectionDialogRepository.currentBinding = binding
        componentRepository.connectionDialogRepository.resetTemplateForBindings()
        componentRepository.connectionDialogRepository.resetLastSelected()
    }

    /**
     * Tell [ConnectionDialogRepository][cz.palda97.lpclient.model.repository.ConnectionDialogRepository]
     * what component to use for preparing it's bindings.
     */
    fun prepareBindings(component: Component) = dbScope.launch {
        componentRepository.connectionDialogRepository.setTemplateForBindings(component)
    }

    /**
     * LiveData with bindings that are not on the same side as a binding user has clicked on to create connection.
     */
    val connectionBindings
        get() = componentRepository.connectionDialogRepository.liveBinding.map {
            if (it.status.result.toStatus != ComponentRepository.StatusCode.OK)
                return@map it
            val ownBinding = componentRepository.connectionDialogRepository.currentBinding
            return@map StatusWithBinding(it.status, it.list.filter {
                !(ownBinding isSameSideAs it)
            })
        }

    /**
     * [ConnectionDialogRepository.liveComponents][cz.palda97.lpclient.model.repository.ConnectionDialogRepository.liveComponents]
     */
    val connectionComponents
        get() = componentRepository.connectionDialogRepository.liveComponents

    /**
     * Creates a connection between the component and currently edited component.
     * @param component Component that is **not** currently edited.
     * @param binding Binding that is **not** currently edited.
     */
    fun saveConnection(component: Component, binding: Binding) {
        val ownBinding = componentRepository.connectionDialogRepository.currentBinding
        val ownComponentId = componentRepository.currentComponentId
        val ownBindingValue = ownBinding.bindingValue
        val pipelineId = pipelineRepository.currentPipelineId
        val newId = IdGenerator.connectionId(pipelineId)
        val connection = when(ownBinding.type) {
            Binding.Type.OUTPUT -> Connection(ownBindingValue, ownComponentId, binding.bindingValue, component.id, emptyList(), newId)
            else -> Connection(binding.bindingValue, component.id, ownBindingValue, ownComponentId, emptyList(), newId)
        }
        dbScope.launch {
            componentRepository.persistConnection(connection)
        }
    }

    /**
     * [ConnectionDialogRepository.lastSelectedComponentPosition][cz.palda97.lpclient.model.repository.ConnectionDialogRepository.lastSelectedComponentPosition]
     */
    var lastSelectedComponentPosition: Int?
        get() = componentRepository.connectionDialogRepository.lastSelectedComponentPosition
        set(value) {
            componentRepository.connectionDialogRepository.lastSelectedComponentPosition = value
        }

    /**
     * [ConnectionDialogRepository.lastSelectedBindingPosition][cz.palda97.lpclient.model.repository.ConnectionDialogRepository.lastSelectedBindingPosition]
     */
    var lastSelectedBindingPosition: Int?
        get() = componentRepository.connectionDialogRepository.lastSelectedBindingPosition
        set(value) {
            componentRepository.connectionDialogRepository.lastSelectedBindingPosition = value
        }
    // -------------------- create connection dialog / ---------------------------

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Gets an instance of [EditComponentViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}
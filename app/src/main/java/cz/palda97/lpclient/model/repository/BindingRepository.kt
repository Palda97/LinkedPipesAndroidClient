package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.viewmodel.editcomponent.BindingComplete
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputContext
import cz.palda97.lpclient.viewmodel.editcomponent.OnlyStatus

/**
 * Repository for bindings and connections.
 * To set current component, call [setImportantIds]
 * @see setImportantIds
 */
class BindingRepository(private val pipelineDao: PipelineDao) {

    private var currentComponentId = ""
    private var currentTemplateId = ""

    private fun liveInputConnections() = pipelineDao.liveInputConnectionsByComponentId(currentComponentId)
    private fun liveOutputConnections() = pipelineDao.liveOutputConnectionsByComponentId(currentComponentId)
    private fun liveComponents() = pipelineDao.liveComponent()

    /**
     * LiveData with current template's bindings.
     */
    fun liveBindings() = pipelineDao.liveBindingWithStatus(currentTemplateId)
    private fun liveOtherBindings() = pipelineDao.liveBindingWithStatus()
    private fun liveTemplates() = pipelineDao.liveTemplate()

    /**
     * Setup current [Binding]s
     * @param componentId [Component]'s id.
     * @param templateId The **root** [Template]'s id.
     */
    fun setImportantIds(componentId: String, templateId: String) {
        currentComponentId = componentId
        currentTemplateId = templateId
    }

    private val storage = object {
        var lastComponentId = ""
        var statusBinding: StatusWithBinding? = null
        var inputConnection: List<Connection>? = null
        var outputConnection: List<Connection>? = null
        var components: List<Component>? = null
        var templates: List<Template>? = null
        var otherBindings: List<StatusWithBinding>? = null
        private fun reset(id: String) {
            lastComponentId = id
            statusBinding = null
            inputConnection = null
            outputConnection = null
            components = null
            templates = null
            otherBindings = null
        }
        fun resetMaybe() {
            if (currentComponentId != lastComponentId) {
                reset(currentComponentId)
            }
        }
    }

    private fun checkForBinding(mediator: MediatorLiveData<ConfigInputContext>): Pair<StatusWithBinding, ComponentRepository.StatusCode>? {
        val statusWithBinding = storage.statusBinding ?: return null
        val status = statusWithBinding.status.result.toStatus
        if (status != ComponentRepository.StatusCode.OK) {
            mediator.value = OnlyStatus(status)
            return null
        }
        return statusWithBinding to status
    }

    private fun mediatorSources(mediator: MediatorLiveData<ConfigInputContext>, updateMediator: () -> Unit) {
        mediator.addSource(liveBindings()) {
            storage.statusBinding = it ?: return@addSource
            updateMediator()
        }
        mediator.addSource(liveComponents()) {
            storage.components = it ?: return@addSource
            updateMediator()
        }
        mediator.addSource(liveTemplates()) {
            storage.templates = it ?: return@addSource
            updateMediator()
        }
        mediator.addSource(liveOtherBindings()) {
            storage.otherBindings = it ?: return@addSource
            updateMediator()
        }
    }

    private enum class Direction {
        INPUT, OUTPUT
    }

    private fun generateUpdateMediatorFun(mediator: MediatorLiveData<ConfigInputContext>, direction: Direction): () -> Unit = fun() {
        val (statusWithBinding, status) = checkForBinding(mediator) ?: return
        val connections = if (direction == Direction.INPUT) {
            storage.inputConnection
        } else {
            storage.outputConnection
        } ?: return
        val components = storage.components ?: return
        val templates = storage.templates ?: return
        val otherBindings = storage.otherBindings ?: return
        mediator.value = BindingComplete(status, statusWithBinding.list, connections, components, templates, otherBindings)
    }

    private fun getInputBindingMediator() = MediatorLiveData<ConfigInputContext>().also { mediator ->

        val updateMediator = generateUpdateMediatorFun(mediator, Direction.INPUT)

        updateMediator()

        mediatorSources(mediator, updateMediator)

        mediator.addSource(liveInputConnections()) {
            storage.inputConnection = it ?: return@addSource
            updateMediator()
        }
    }

    private fun getOutputBindingMediator() = MediatorLiveData<ConfigInputContext>().also { mediator ->

        val updateMediator = generateUpdateMediatorFun(mediator, Direction.OUTPUT)

        updateMediator()

        mediatorSources(mediator, updateMediator)

        mediator.addSource(liveOutputConnections()) {
            storage.outputConnection = it ?: return@addSource
            updateMediator()
        }
    }

    /**
     * LiveData with current component's input connections.
     * (including configuration)
     * @see BindingComplete
     */
    val liveInputContext: LiveData<ConfigInputContext>
        get() = synchronized(this) {
            storage.resetMaybe()
            return@synchronized getInputBindingMediator()
        }

    /**
     * LiveData with current component's output connections.
     * @see BindingComplete
     */
    val liveOutputContext: LiveData<ConfigInputContext>
        get() = synchronized(this) {
            storage.resetMaybe()
            return@synchronized getOutputBindingMediator()
        }
}
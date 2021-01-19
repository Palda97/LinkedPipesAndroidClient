package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.viewmodel.editcomponent.BindingComplete
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputContext
import cz.palda97.lpclient.viewmodel.editcomponent.OnlyStatus

class BindingRepository(private val pipelineDao: PipelineDao) {

    private var currentComponentId = ""
    private var currentTemplateId = ""

    private fun liveInputConnections() = pipelineDao.liveInputConnectionsByComponentId(currentComponentId)
    private fun liveOutputConnections() = pipelineDao.liveOutputConnectionsByComponentId(currentComponentId)
    fun liveBindings() = pipelineDao.liveBindingWithStatus(currentTemplateId)

    fun setImportantIds(componentId: String, templateId: String) {
        currentComponentId = componentId
        currentTemplateId = currentTemplateId
    }

    private val storage = object {
        var lastComponentId = ""
        var statusBinding: StatusWithBinding? = null
        var inputConnection: List<Connection>? = null
        var outputConnection: List<Connection>? = null
        private fun reset(id: String) {
            lastComponentId = id
            statusBinding = null
            inputConnection = null
            outputConnection = null
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

    private fun getInputBindingMediator() = MediatorLiveData<ConfigInputContext>().also { mediator ->

        fun updateMediator() {
            val (statusWithBinding, status) = checkForBinding(mediator) ?: return
            val connections = storage.inputConnection ?: return
            mediator.value = BindingComplete(status, statusWithBinding.list, connections)
        }

        updateMediator()

        mediator.addSource(liveInputConnections()) {
            storage.inputConnection = it ?: return@addSource
            updateMediator()
        }
        mediator.addSource(liveBindings()) {
            storage.statusBinding = it ?: return@addSource
            updateMediator()
        }
    }

    private fun getOutputBindingMediator() = MediatorLiveData<ConfigInputContext>().also { mediator ->

        fun updateMediator() {
            val (statusWithBinding, status) = checkForBinding(mediator) ?: return
            val connections = storage.outputConnection ?: return
            mediator.value = BindingComplete(status, statusWithBinding.list, connections)
        }

        updateMediator()

        mediator.addSource(liveOutputConnections()) {
            storage.outputConnection = it ?: return@addSource
            updateMediator()
        }
        mediator.addSource(liveBindings()) {
            storage.statusBinding = it ?: return@addSource
            updateMediator()
        }
    }

    val liveInputContext: LiveData<ConfigInputContext>
        get() = synchronized(this) {
            storage.resetMaybe()
            return@synchronized getInputBindingMediator()
        }

    val liveOutputContext: LiveData<ConfigInputContext>
        get() = synchronized(this) {
            storage.resetMaybe()
            return@synchronized getOutputBindingMediator()
        }
}
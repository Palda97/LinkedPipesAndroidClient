package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.entities.pipeline.Binding
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.ConfigDownloadStatus
import cz.palda97.lpclient.model.entities.pipeline.StatusWithBinding
import cz.palda97.lpclient.model.repository.ComponentRepository.Companion.getRootTemplateId

/**
 * This repository is used by [CreateConnectionDialog][cz.palda97.lpclient.view.editcomponent.CreateConnectionDialog].
 */
class ConnectionDialogRepository(private val pipelineDao: PipelineDao) {

    var currentComponentId = ""
        private set

    /**
     * Setup current [Component].
     */
    var currentComponent: Component? = null
        set(value) {
            field = value
            value?.let {
                currentComponentId = it.id
            }
        }

    /**
     * LiveData with all pipeline's components except the current one.
     */
    val liveComponents
        get() = pipelineDao.liveComponentExceptThisOne(currentComponentId)

    private val templateHolder = MutableLiveData<String>()

    /**
     * Resets root template for [liveBinding].
     */
    fun resetTemplateForBindings() {
        templateHolder.value = null
    }

    /**
     * Sets root template for [liveBinding].
     * @param component [Component] for finding the root template.
     */
    suspend fun setTemplateForBindings(component: Component) {
        val rootTemplate = component.getRootTemplateId(pipelineDao)
        templateHolder.postValue(rootTemplate)
    }

    /**
     * LiveData with [StatusWithBinding]s belonging to root template of selected component.
     * (Not the current one)
     */
    val liveBinding: LiveData<StatusWithBinding> = templateHolder.switchMap {
        val template = it ?: return@switchMap MutableLiveData<StatusWithBinding>().apply {
            value = StatusWithBinding(ConfigDownloadStatus("", ConfigDownloadStatus.TYPE_BINDING, ""), emptyList())
        }
        pipelineDao.liveBindingWithStatus(template)
    }

    /**
     * [Binding] that has been clicked on by user to add connection to.
     */
    lateinit var currentBinding: Binding

    /**
     * Position of selected [Component] in [CreateConnectionDialog][cz.palda97.lpclient.view.editcomponent.CreateConnectionDialog].
     */
    var lastSelectedComponentPosition: Int? = null

    /**
     * Position of selected [Binding] in [CreateConnectionDialog][cz.palda97.lpclient.view.editcomponent.CreateConnectionDialog].
     */
    var lastSelectedBindingPosition: Int? = null

    /**
     * Resets the positions of selected [Component] and [Binding].
     */
    fun resetLastSelected() {
        lastSelectedComponentPosition = null
        lastSelectedBindingPosition = null
    }
}
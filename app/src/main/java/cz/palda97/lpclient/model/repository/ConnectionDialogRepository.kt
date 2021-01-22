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

class ConnectionDialogRepository(private val pipelineDao: PipelineDao) {

    var currentComponentId = ""
        private set

    var currentComponent: Component? = null
        set(value) {
            field = value
            value?.let {
                currentComponentId = it.id
            }
        }

    val liveComponents
        get() = pipelineDao.liveComponentExceptThisOne(currentComponentId)

    private val templateHolder = MutableLiveData<String>()

    fun resetTemplateForBindings() {
        templateHolder.value = null
    }

    suspend fun setTemplateForBindings(component: Component) {
        val rootTemplate = component.getRootTemplateId(pipelineDao)
        templateHolder.postValue(rootTemplate)
    }

    val liveBinding: LiveData<StatusWithBinding> = templateHolder.switchMap {
        val template = it ?: return@switchMap MutableLiveData<StatusWithBinding>().apply {
            value = StatusWithBinding(ConfigDownloadStatus("", ConfigDownloadStatus.TYPE_BINDING, ""), emptyList())
        }
        pipelineDao.liveBindingWithStatus(template)
    }

    lateinit var currentBinding: Binding
}
package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Configuration
import cz.palda97.lpclient.model.entities.pipeline.StatusWithConfigInput
import cz.palda97.lpclient.model.entities.pipeline.StatusWithDialogJs
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputComplete
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputContext
import cz.palda97.lpclient.viewmodel.editcomponent.OnlyStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConfigurationRepository(private val pipelineDao: PipelineDao) {

    private var currentComponentId = ""
    private var currentConfigurationId = ""

    private fun liveConfigInput(componentId: String = currentComponentId) = pipelineDao.liveConfigWithStatus(componentId)
    private fun liveDialogJs(componentId: String = currentComponentId) = pipelineDao.liveDialogJsWithStatus(componentId)
    private fun liveConfiguration(configurationId: String = currentConfigurationId) = pipelineDao.liveConfigurationById(configurationId)

    var currentComponent: Component? = null
        set(value) {
            field = value
            value?.let {
                currentComponentId = it.id
                currentConfigurationId = it.configurationId ?: ""
            }
        }

    private val configStorage = object {
        var lastComponentId = ""
        var statusConfigInput: StatusWithConfigInput? = null
        var statusDialogJs: StatusWithDialogJs? = null
        val configurationMap = mutableMapOf<String, Configuration>()
        fun reset(id: String) {
            lastComponentId = id
            statusConfigInput = null
            statusDialogJs = null
        }
    }

    private fun getConfigInputMediator() = MediatorLiveData<ConfigInputContext>().also { mediator ->

        fun updateConfigInputMediator() {
            configStorage.statusDialogJs?.let {
                if (configStorage.statusConfigInput == null) {
                    mediator.value = OnlyStatus(it.status.result.toStatus)
                }
            }
            val sConfigInput = configStorage.statusConfigInput ?: return
            val sConfigInputStatus = sConfigInput.status.result.toStatus
            if (sConfigInputStatus != ComponentRepository.StatusCode.OK) {
                mediator.value = OnlyStatus(sConfigInputStatus)
                return
            }
            val sDialog = configStorage.statusDialogJs ?: return
            val sDialogStatus = sDialog.status.result.toStatus
            if (sDialogStatus != ComponentRepository.StatusCode.OK) {
                mediator.value = OnlyStatus(sDialogStatus)
                return
            }
            if (sDialog.dialogJs == null) {
                mediator.value = OnlyStatus(ComponentRepository.StatusCode.INTERNAL_ERROR)
                return
            }
            if (configStorage.configurationMap[currentComponentId] == null)
                return
            mediator.value = ConfigInputComplete(
                ComponentRepository.StatusCode.OK,
                sDialog.dialogJs,
                sConfigInput.list
            )
        }

        updateConfigInputMediator()

        mediator.addSource(liveConfigInput()) {
            if (configStorage.statusConfigInput?.status?.result.toStatus == ComponentRepository.StatusCode.OK)
                return@addSource
            configStorage.statusConfigInput = it ?: return@addSource
            updateConfigInputMediator()
        }
        mediator.addSource(liveDialogJs()) {
            if (configStorage.statusDialogJs?.status?.result.toStatus == ComponentRepository.StatusCode.OK)
                return@addSource
            configStorage.statusDialogJs = it ?: return@addSource
            updateConfigInputMediator()
        }
        mediator.addSource(liveConfiguration()) {
            if (configStorage.configurationMap[currentComponentId] != null)
                return@addSource
            configStorage.configurationMap[currentComponentId] = it ?: return@addSource
            updateConfigInputMediator()
        }
    }

    private val updateConfigurationMutex = Mutex()
    suspend fun updateConfiguration(componentId: String) = updateConfigurationMutex.withLock {
        val configuration = configStorage.configurationMap[componentId] ?: return@withLock
        pipelineDao.insertConfiguration(configuration)
    }

    val liveConfigInputContext: LiveData<ConfigInputContext>
        get() = synchronized(this) {
            if (currentComponentId != configStorage.lastComponentId) {
                configStorage.reset(currentComponentId)
            }
            return@synchronized getConfigInputMediator()
        }

    fun getString(key: String) = configStorage.configurationMap[currentComponentId]?.getString(key)
    fun setString(key: String, value: String) = configStorage.configurationMap[currentComponentId]?.setString(key, value)
}
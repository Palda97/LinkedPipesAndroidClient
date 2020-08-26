package cz.palda97.lpclient.viewmodel.editserver

import android.util.Log
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.repository.ServerRepository

class EditServerViewModel : ViewModel() {

    private val serverRepository = Injector.serverRepository
    private val editServerRepository = Injector.editServerRepository

    fun resetSaveStatus() {
        _saveSuccessful.value =
            SaveStatus.WAITING
    }

    init {
        Log.d(TAG, "init")
    }

    private fun saveAndClean(serverInstance: ServerInstance) {
        Log.d(TAG, "saveAndClean(${serverInstance.name})")
        editServerRepository.forgetTmpServer()
        if (editServerRepository.rewrite)
            serverRepository.deleteAndCreate(serverRepository.serverToEdit.value!!, serverInstance)
        else
            serverRepository.saveServer(serverInstance)
    }

    private val _saveSuccessful = MediatorLiveData<SaveStatus>()
    val saveSuccessful: LiveData<SaveStatus>
        get() = _saveSuccessful

    fun saveServer(serverInstance: ServerInstance = tmpServerInstance) {
        val attributeCheck = ServerInstanceAttributeCheck(serverInstance)
        editServerRepository.doneButtonEnable.value = !attributeCheck.isNameAndUrlOk
        l("saveServer(${serverInstance.name}) - isNameAndUrlOk = ${attributeCheck.isNameAndUrlOk}")
        val liveStatus: LiveData<SaveStatus> = if (!attributeCheck.isNameAndUrlOk) {
            attributeCheck.liveData
        } else {
            matchingInstance(serverInstance)
        }
        _saveSuccessful.addSource(liveStatus) { _saveSuccessful.value = it }
    }

    private fun matchingInstance(
        serverInstance: ServerInstance
    ): LiveData<SaveStatus> = Transformations.map(
        if (serverRepository.serverToEdit.value == null || serverRepository.serverToEdit.value == ServerInstance()) serverRepository.matchingUrlAndName(
            serverInstance
        ) else serverRepository.matchingUrlExcept(
            serverInstance,
            serverRepository.serverToEdit.value!!
        )
    ) {
        if (it == null)
            return@map SaveStatus.WAITING
        Log.d(TAG, "Match arrived: ${it.mailContent!!.name}")
        if (!it.isLoading)
            editServerRepository.doneButtonEnable.value = true
        if (it.isOk) {
            it.mailContent!!
            when (it.mailContent) {
                ServerRepository.MatchCases.URL -> return@map SaveStatus.URL
                ServerRepository.MatchCases.NAME -> return@map SaveStatus.NAME
            }
        }
        if (it.isError) {
            saveAndClean(serverInstance)
            return@map SaveStatus.OK
        }
        return@map SaveStatus.WAITING
    }

    val doneButtonEnable: LiveData<Boolean>
        get() = editServerRepository.doneButtonEnable

    var tmpServerInstance: ServerInstance
        get() = editServerRepository.tmpServerInstance
        set(value) {
            editServerRepository.tmpServerInstance = value
        }

    companion object {
        private const val TAG = "EditServerViewModel"
        private fun l(msg: String) {
            Log.d(TAG, msg)
        }
    }

    enum class SaveStatus {
        NAME, URL, OK, WAITING, EMPTY_NAME, EMPTY_URL
    }
}
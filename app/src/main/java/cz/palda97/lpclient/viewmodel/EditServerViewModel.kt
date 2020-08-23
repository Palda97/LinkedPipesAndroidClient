package cz.palda97.lpclient.viewmodel

import android.util.Log
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.StatusPackage
import cz.palda97.lpclient.model.repository.ServerRepository

class EditServerViewModel : ViewModel() {

    private val serverRepository = Injector.serverRepository
    private val editServerRepository = Injector.editServerRepository

    fun resetSaveStatus() {
        _saveSuccessful.value = SaveStatus.WAITING
    }

    init {
        Log.d(TAG, "init")
    }

    private fun saveAndClean(serverInstance: ServerInstance) {
        editServerRepository.forgetTmpServer()
        serverRepository.saveServer(serverInstance)
    }

    private val _saveSuccessful = MediatorLiveData<SaveStatus>()
    val saveSuccessful: LiveData<SaveStatus>
        get() = _saveSuccessful

    fun saveServer(serverInstance: ServerInstance = tmpServerInstance) {
        editServerRepository.doneButtonEnable.value =
            serverInstance.name.isEmpty() || serverInstance.url.isEmpty()
        val liveStatus: LiveData<SaveStatus> = if (serverInstance.name.isEmpty()) {
            MutableLiveData(SaveStatus.EMPTY_NAME)
        } else if (serverInstance.url.isEmpty()) {
            MutableLiveData(SaveStatus.EMPTY_URL)
        } else {
            matchingInstance(serverInstance)
        }
        _saveSuccessful.addSource(liveStatus) { _saveSuccessful.value = it }
    }

    private fun matchingInstance(
        serverInstance: ServerInstance
    ): LiveData<SaveStatus> = Transformations.map(
        if (serverRepository.serverToEdit.value!! == ServerInstance()) serverRepository.matchingUrlAndName(
            serverInstance
        ) else serverRepository.matchingUrlExcept(
            serverInstance,
            serverRepository.serverToEdit.value!!
        )
    ) {
        if (it == null)
            return@map SaveStatus.WAITING
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
    }

    enum class SaveStatus {
        NAME, URL, OK, WAITING, EMPTY_NAME, EMPTY_URL
    }
}
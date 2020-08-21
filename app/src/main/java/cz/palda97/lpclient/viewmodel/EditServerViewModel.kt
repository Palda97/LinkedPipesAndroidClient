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

    //private val _saveSuccessful = MutableLiveData<SaveStatus>(SaveStatus.WAITING)
    //val saveSuccessful: LiveData<SaveStatus>
    //    get() = _saveSuccessful

    fun resetSaveStatus() {
        _saveSuccessful.value = SaveStatus.WAITING
    }

    init {
        Log.d(TAG, "init")
    }

    /*fun saveServer(serverInstance: ServerInstance = tmpServerInstance) {
        //serverRepository.saveServer(serverInstance)
        //editServerRepository.forgetTmpServer()
        if (!editServerRepository.rewrite) {
            TODO("learn how to transform observables")
        }
        editServerRepository.forgetTmpServer()
        serverRepository.saveServer(serverInstance)
        _saveSuccessful.value = SaveStatus.OK
    }*/

    private fun saveAndClean(serverInstance: ServerInstance) {
        editServerRepository.forgetTmpServer()
        serverRepository.saveServer(serverInstance)
    }

    private val _saveSuccessful = MediatorLiveData<SaveStatus>()
    val saveSuccessful: LiveData<SaveStatus>
        get() = _saveSuccessful

    fun saveServer(serverInstance: ServerInstance = tmpServerInstance) {
        val liveStatus: LiveData<SaveStatus> = if (!editServerRepository.rewrite) {
            Transformations.map(serverRepository.matchingUrlAndName(serverInstance)) {
                if (it == null)
                    return@map null
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
        } else {
            saveAndClean(serverInstance)
            MutableLiveData(SaveStatus.OK)
        }
        _saveSuccessful.addSource(liveStatus) { _saveSuccessful.value = it }
    }

    var tmpServerInstance: ServerInstance
        get() = editServerRepository.tmpServerInstance
        set(value) {
            editServerRepository.tmpServerInstance = value
        }

    companion object {
        private const val TAG = "EditServerViewModel"
    }

    enum class SaveStatus {
        NAME, URL, OK, WAITING
    }
}
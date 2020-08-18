package cz.palda97.lpclient.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.repository.ServerRepository

class EditServerViewModel : ViewModel() {

    private val serverRepository = Injector.serverRepository
    private val editServerRepository = Injector.editServerRepository

    init {
        Log.d(TAG, "init")
    }

    val serverToEdit: LiveData<ServerInstance>
        get() = serverRepository.serverToEdit

    fun saveServer(serverInstance: ServerInstance = tmpServerInstance) {
        serverRepository.saveServer(serverInstance)
        editServerRepository.forgetTmpServer()
    }

    var tmpServerInstance: ServerInstance
        get() = editServerRepository.tmpServerInstance
        set(value) {
            editServerRepository.tmpServerInstance = value
        }

    companion object {
        private const val TAG = "EditServerViewModel"
    }
}
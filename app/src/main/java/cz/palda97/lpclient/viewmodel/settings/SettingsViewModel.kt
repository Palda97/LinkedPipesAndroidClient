package cz.palda97.lpclient.viewmodel.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.repository.EditServerRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = SharedPreferencesFactory.sharedPreferences(application)
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val editServerRepository: EditServerRepository = Injector.editServerRepository

    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    private var lastDeletedServer: ServerInstance = ServerInstance()

    init {
        l("init")
    }

    var notifications: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS, false)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS, value).apply()

    fun deleteAllInstances() {
        dbScope.launch {
            serverRepository.deleteAll()
        }
    }

    fun forceSaveServer(serverInstance: ServerInstance) {
        dbScope.launch {
            serverRepository.insertServer(serverInstance)
        }
    }

    fun deleteServer(serverInstance: ServerInstance) {
        lastDeletedServer = serverInstance
        dbScope.launch {
            serverRepository.deleteServer(serverInstance)
        }
    }

    fun undoLastDeleteServer() {
        forceSaveServer(lastDeletedServer)
    }

    val liveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.liveServers

    fun editServer(serverInstance: ServerInstance) {
        editServerRepository.serverToEdit = serverInstance
        editServerRepository.tmpServer = serverInstance
    }

    fun addServer() {
        editServer(ServerInstance())
    }

    fun findServerByName(name: String?): ServerInstance? = liveServers.value?.mailContent?.find {
        it.name == name
    }

    var serverToFilter: ServerInstance?
        get() = serverRepository.serverToFilter
        set(value) {
            serverRepository.serverToFilter = value
        }

    companion object {
        private const val NOTIFICATIONS = "notifications"
        private const val TAG = "SettingsViewModel"
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}
package cz.palda97.lpclient.viewmodel.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.repository.EditServerRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import cz.palda97.lpclient.view.Notifications.NOTIFICATIONS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = SharedPreferencesFactory.sharedPreferences(application)
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val editServerRepository: EditServerRepository = Injector.editServerRepository

    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    var nightMode: NightModeEnum
        get() = NightModeEnum.fromNightMode(
            sharedPreferences.getInt(AppInit.NIGHT_MODE, AppInit.DEFAULT_NIGHT_MODE)
        )
        set(value) {
            sharedPreferences.edit().putInt(AppInit.NIGHT_MODE, value.nightMode).apply()
            AppCompatDelegate.setDefaultNightMode(value.nightMode)
        }

    private var lastDeletedServer: ServerInstance =
        ServerInstance()

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

    private fun forceSaveServer(serverInstance: ServerInstance) {
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

    val activeLiveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.activeLiveServers

    fun editServer(serverInstance: ServerInstance) {
        editServerRepository.serverToEdit = serverInstance
        editServerRepository.tmpServer = serverInstance
    }

    fun addServer() {
        editServer(ServerInstance())
    }

    fun findActiveServerByName(name: String?): ServerInstance? =
        activeLiveServers.value?.mailContent?.find {
            it.name == name
        }

    fun activeChange(server: ServerInstance) {
        server.active = !server.active
        forceSaveServer(server)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(SettingsViewModel::class.java)
    }
}
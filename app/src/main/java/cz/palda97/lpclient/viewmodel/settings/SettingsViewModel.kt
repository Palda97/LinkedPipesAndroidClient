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
import cz.palda97.lpclient.model.services.ExecutionMonitorPeriodic
import cz.palda97.lpclient.view.Notifications.NOTIFICATIONS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the [SettingsFragment][cz.palda97.lpclient.view.settings.SettingsFragment].
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = SharedPreferencesFactory.sharedPreferences(application)
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val editServerRepository: EditServerRepository = Injector.editServerRepository

    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    /**
     * A property for getting and setting the night mode.
     */
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

    /**
     * A property for turning notifications on and off and getting this settings.
     */
    var notifications: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS, false)
        set(value) {
            sharedPreferences.edit().putBoolean(NOTIFICATIONS, value).apply()
            when(value) {
                true -> ExecutionMonitorPeriodic.enqueue(getApplication())
                false -> ExecutionMonitorPeriodic.cancel(getApplication())
            }
        }

    private fun forceSaveServer(serverInstance: ServerInstance) {
        dbScope.launch {
            serverRepository.insertServer(serverInstance)
        }
    }

    /**
     * Delete this server instance from database.
     * (Also, store it locally for the UNDO option)
     */
    fun deleteServer(serverInstance: ServerInstance) {
        lastDeletedServer = serverInstance
        dbScope.launch {
            serverRepository.deleteServer(serverInstance)
        }
    }

    /**
     * UNDO the last deletion of a server instance.
     */
    fun undoLastDeleteServer() {
        forceSaveServer(lastDeletedServer)
    }

    /** @see ServerRepository.liveServers */
    val liveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.liveServers

    /** @see ServerRepository.activeLiveServers */
    val activeLiveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.activeLiveServers

    /**
     * Prepare [EditServerRepository] for this server instance.
     */
    fun editServer(serverInstance: ServerInstance) {
        editServerRepository.serverToEdit = serverInstance
        editServerRepository.tmpServer = serverInstance
    }

    /**
     * Prepare [EditServerRepository] for a new server instance.
     */
    fun addServer() {
        editServer(ServerInstance())
    }

    /**
     * Find active server by name.
     * @return [ServerInstance] or null if not found.
     */
    fun findActiveServerByName(name: String?): ServerInstance? =
        activeLiveServers.value?.mailContent?.find {
            it.name == name
        }

    /**
     * Change the active property of this [ServerInstance].
     */
    fun activeChange(server: ServerInstance) {
        server.active = !server.active
        forceSaveServer(server)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Gets an instance of [SettingsViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(SettingsViewModel::class.java)
    }
}
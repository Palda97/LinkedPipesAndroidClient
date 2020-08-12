package cz.palda97.lpclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.repository.ServerRepository
import cz.palda97.lpclient.model.repository.ServerRepositoryFake

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val sharedPreferences = SharedPreferencesFactory.sharedPreferences(application)

    var notifications: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS, false)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS, value).apply()

    fun deleteServer(serverInstance: ServerInstance) {
        TODO("delete server instance in settings viewmodel")
    }

    fun saveServer(serverInstance: ServerInstance) {
        TODO("save server instance in settings viewmodel")
    }

    fun editServer(serverInstance: ServerInstance) {
        TODO("edit server instance in settings viewmodel")
    }

    private val serverRepository: ServerRepository = ServerRepositoryFake()

    val liveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.liveServers

    companion object {
        private const val NOTIFICATIONS = "notifications"
    }
}
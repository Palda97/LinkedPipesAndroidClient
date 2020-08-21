package cz.palda97.lpclient.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.repository.EditServerRepository
import cz.palda97.lpclient.model.repository.ServerRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = SharedPreferencesFactory.sharedPreferences(application)
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val editServerRepository: EditServerRepository = Injector.editServerRepository

    init {
        Log.d(TAG, "init")
    }

    var notifications: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS, false)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS, value).apply()

    fun deleteServer(serverInstance: ServerInstance) {
        TODO("delete server instance in settings viewmodel")
    }

    fun editServer(serverInstance: ServerInstance) {
        editServerRepository.rewrite = serverInstance != ServerInstance()
        editServerRepository.tmpServerInstance = serverInstance
    }

    val liveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.liveServers

    companion object {
        private const val NOTIFICATIONS = "notifications"
        private const val TAG = "SettingsViewModel"
    }
}
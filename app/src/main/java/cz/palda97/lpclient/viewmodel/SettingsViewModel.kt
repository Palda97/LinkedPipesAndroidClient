package cz.palda97.lpclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cz.palda97.lpclient.model.SharedPreferencesFactory

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val sharedPreferences = SharedPreferencesFactory.sharedPreferences(application)

    var notifications: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS, false)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS, value).apply()

    companion object {
        private const val NOTIFICATIONS = "notifications"
    }
}
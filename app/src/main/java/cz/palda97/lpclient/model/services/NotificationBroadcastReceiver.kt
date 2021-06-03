package cz.palda97.lpclient.model.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.view.Notifications

@SuppressLint("ApplySharedPref")
class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }
        val sharedPreferences = SharedPreferencesFactory.sharedPreferences(context)
        sharedPreferences.edit()
            .putBoolean(Notifications.NOTIFICATIONS_CANCEL, true)
            .putBoolean(Notifications.NOTIFICATIONS, false)
            .commit()
        ExecutionMonitorPeriodic.cancel(context)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
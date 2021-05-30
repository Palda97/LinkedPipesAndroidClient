package cz.palda97.lpclient

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.db.AppDatabase
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.viewmodel.CommonViewModel
import kotlinx.coroutines.*
import org.conscrypt.Conscrypt
import java.security.Security

class AppInit : Application() {

    /**
     * This runs on the app start.
     * It checks if every delete request has been sent,
     * updates executions and pipelines
     * and registers the notification channel.
     *
     * It also calls init function.
     * @see init
     */
    override fun onCreate() {
        super.onCreate()
        init(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            cleanDb()
            refresh()
        }
        notificationChannels()
    }

    /**
     * Registers notification channels.
     */
    private fun notificationChannels() {
        notificationChannelDefault()
        notificationChannelForeground()
    }

    /**
     * Registers a default notification channel for execution notifications.
     */
    private fun notificationChannelDefault() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            //val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                //description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Registers a notification channel for the persistent foreground notification.
     */
    private fun notificationChannelForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name_foreground)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_FOREGROUND, name, importance)
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Constant for shared preferences.
         */
        const val NIGHT_MODE = "NIGHT_MODE"

        /**
         * Default night mode settings.
         */
        const val DEFAULT_NIGHT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        /**
         * Gives Injector context,
         * registers Conscrypt provider for ssl communication,
         * sets night mode according to settings.
         */
        fun init(context: Context) {
            if (Injector.isThereContext)
                return
            Injector.context = context
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
            val nightMode = SharedPreferencesFactory.sharedPreferences(context)
                .getInt(NIGHT_MODE, DEFAULT_NIGHT_MODE)
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        private fun noContext(context: Context?): Boolean {
            if (!Injector.isThereContext) {
                if (context == null)
                    return true
                Injector.context = context
            }
            return false
        }

        private suspend fun <R> afterInit(context: Context?, block: suspend () -> R): R? =
            if (noContext(context))
                null
            else
                block()

        /**
         * @see RepositoryRoutines.cleanDb
         */
        suspend fun cleanDb(context: Context? = null) = afterInit(context) {
            RepositoryRoutines().cleanDb()
        }

        /**
         * @see RepositoryRoutines.refresh
         */
        suspend fun refresh(context: Context? = null) = afterInit(context) {
            CommonViewModel.refreshAndNotify()
        }

        /**
         * Channel id for notification channel.
         */
        const val CHANNEL_ID = "etl_client_channel"
        const val CHANNEL_ID_FOREGROUND = "etl_client_channel_foreground"
    }
}
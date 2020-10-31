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
import kotlinx.coroutines.*
import org.conscrypt.Conscrypt
import java.security.Security

class AppInit : Application() {
    override fun onCreate() {
        super.onCreate()
        init(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            cleanDb()
            refresh()
        }
        notificationChannel()
    }

    private fun notificationChannel() {
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

    companion object {
        private val l = Injector.generateLogFunction(this)

        const val NIGHT_MODE = "NIGHT_MODE"
        const val DEFAULT_NIGHT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

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

        suspend fun cleanDb(context: Context? = null) = afterInit(context) {
            RepositoryRoutines().cleanDb()
        }

        suspend fun preserveOnlyServers(context: Context? = null) = afterInit(context) {
            val db = AppDatabase.getInstance(Injector.context)
            val pipelineViewDao = db.pipelineViewDao()
            val executionDao = db.executionDao()
            pipelineViewDao.deleteAll()
            executionDao.deleteAll()
        }

        suspend fun refresh(context: Context? = null) = afterInit(context) {
            RepositoryRoutines().refresh()
        }

        const val CHANNEL_ID = "etl_client_channel"
    }
}
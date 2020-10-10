package cz.palda97.lpclient

import android.app.Application
import android.content.Context
import android.util.Log
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
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)

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
    }
}
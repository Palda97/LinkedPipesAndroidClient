package cz.palda97.lpclient

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import cz.palda97.lpclient.model.SharedPreferencesFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.conscrypt.Conscrypt
import java.security.Security

class AppInit : Application() {
    override fun onCreate() {
        super.onCreate()
        init(applicationContext)

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val uiModeNight = when(applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> "yes"
            Configuration.UI_MODE_NIGHT_NO -> "no"
            else -> "else"
        }
        l("system night mode: $uiModeNight")

        val appCompat = when(AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> "MODE_NIGHT_YES"
            AppCompatDelegate.MODE_NIGHT_NO -> "MODE_NIGHT_NO"
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> "MODE_NIGHT_AUTO_BATTERY"
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "MODE_NIGHT_FOLLOW_SYSTEM"
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> "MODE_NIGHT_UNSPECIFIED"
            else -> "else"
        }
        l("app night mode: $appCompat")

        cleanDb()
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)

        const val NIGHT_MODE = "NIGHT_MODE"
        const val DEFAULT_NIGHT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        fun init(context: Context) {
            Injector.context = context
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
            val nightMode = SharedPreferencesFactory.sharedPreferences(context).getInt(NIGHT_MODE, DEFAULT_NIGHT_MODE)
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

        fun cleanDb(context: Context? = null): Boolean {
            if (noContext(context))
                return false
            CoroutineScope(Dispatchers.IO).launch {
                launch {
                    Injector.pipelineRepository.cleanDb()
                    l("pipelineRepository.cleanDb() should be completed")
                }
                launch {
                    Injector.executionRepository.cleanDb()
                    l("executionRepository.cleanDb() should be completed")
                }
            }
            return true
        }
    }
}
package cz.palda97.lpclient

import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.conscrypt.Conscrypt
import java.security.Security

class AppInit : Application() {
    override fun onCreate() {
        super.onCreate()
        init(applicationContext)
        cleanDb()
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)

        fun init(context: Context) {
            Injector.context = context
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
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
                Injector.pipelineRepository.cleanDb()
                l("cleanDb should be completed")
            }
            return true
        }
    }
}
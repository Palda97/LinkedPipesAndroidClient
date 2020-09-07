package cz.palda97.lpclient

import android.app.Application
import android.content.Context
import org.conscrypt.Conscrypt
import java.security.Security

class AppInit : Application() {
    override fun onCreate() {
        super.onCreate()
        init(applicationContext)
    }

    companion object {
        fun init(context: Context) {
            Injector.context = context
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        }
    }
}
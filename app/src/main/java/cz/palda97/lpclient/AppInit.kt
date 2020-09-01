package cz.palda97.lpclient

import android.app.Application

class AppInit: Application() {
    override fun onCreate() {
        super.onCreate()
        Injector.context = applicationContext
    }
}
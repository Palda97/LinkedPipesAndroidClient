package cz.palda97.lpclient

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class NoApplicationRunner : AndroidJUnitRunner() {
    @Throws(
        InstantiationException::class,
        IllegalAccessException::class,
        ClassNotFoundException::class
    )
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, NoApplication::class.java.getName(), context)
    }
}

class NoApplication : Application()
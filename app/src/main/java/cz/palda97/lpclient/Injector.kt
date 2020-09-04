package cz.palda97.lpclient

import android.content.Context
import cz.palda97.lpclient.model.db.AppDatabase
import cz.palda97.lpclient.model.repository.EditServerRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import cz.palda97.lpclient.model.repository.ServerRepositoryImp

object Injector {
    //var context: Context? = null
    lateinit var context: Context
    val serverRepository: ServerRepository by lazy {
        //ServerRepositoryFake()
        /*while (context == null) {
            Thread.sleep(100)
        }
        ServerRepositoryImp(AppDatabase.getInstance(context!!).serverDao())*/
        ServerRepositoryImp(AppDatabase.getInstance(context).serverDao())
    }
    val editServerRepository: EditServerRepository by lazy {
        EditServerRepository()
    }

    fun tag(companion: Any): String = companion::class.java.declaringClass?.canonicalName.toString().split(".").reversed()[0]
}
package cz.palda97.lpclient

import android.content.Context
import cz.palda97.lpclient.model.db.AppDatabase
import cz.palda97.lpclient.model.db.dao.ExecutionDao
import cz.palda97.lpclient.model.repository.*

object Injector {
    lateinit var context: Context
    val isThereContext: Boolean
        get() = this::context.isInitialized
    val serverRepository: ServerRepository by lazy {
        ServerRepositoryImp(AppDatabase.getInstance(context).serverDao())
    }
    val editServerRepository: EditServerRepository by lazy {
        EditServerRepository()
    }
    val pipelineRepository: PipelineRepository by lazy {
        val db = AppDatabase.getInstance(context)
        PipelineRepository(db.pipelineViewDao(), db.serverDao())
    }
    val executionRepository: ExecutionRepository by lazy {
        ExecutionRepository(AppDatabase.getInstance(context).executionDao())
    }

    fun tag(companion: Any): String =
        companion::class.java.declaringClass?.canonicalName.toString().split(".").last()
}
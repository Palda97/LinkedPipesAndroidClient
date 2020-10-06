package cz.palda97.lpclient

import android.content.Context
import cz.palda97.lpclient.model.db.AppDatabase
import cz.palda97.lpclient.model.repository.*

object Injector {
    lateinit var context: Context
    val isThereContext: Boolean
        get() = this::context.isInitialized
    val serverRepository: ServerRepository by lazy {
        ServerRepository(AppDatabase.getInstance(context).serverDao())
    }
    val editServerRepository: EditServerRepository by lazy {
        EditServerRepository()
    }
    val pipelineRepository: PipelineRepository by lazy {
        val db = AppDatabase.getInstance(context)
        PipelineRepository(db.pipelineViewDao(), db.serverDao(), db.markForDeletionDao())
    }
    val executionRepository: ExecutionRepository by lazy {
        val db = AppDatabase.getInstance(context)
        ExecutionRepository(db.executionDao(), db.serverDao(), db.markForDeletionDao())
    }

    fun tag(companion: Any): String =
        companion::class.java.declaringClass?.canonicalName.toString().split(".").last()
}
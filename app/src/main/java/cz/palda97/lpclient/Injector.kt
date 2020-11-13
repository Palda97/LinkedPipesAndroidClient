package cz.palda97.lpclient

import android.content.Context
import android.util.Log
import cz.palda97.lpclient.model.SharedPreferencesFactory
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
    val pipelineViewRepository: PipelineViewRepository by lazy {
        val db = AppDatabase.getInstance(context)
        PipelineViewRepository(db.pipelineViewDao(), db.serverDao(), db.markForDeletionDao())
    }
    val executionRepository: ExecutionRepository by lazy {
        val db = AppDatabase.getInstance(context)
        ExecutionRepository(db.executionDao(), db.serverDao(), db.markForDeletionDao())
    }
    val pipelineRepository: PipelineRepository by lazy {
        val db = AppDatabase.getInstance(context)
        val sharedPreferences = SharedPreferencesFactory.sharedPreferences(context)
        PipelineRepository(db.serverDao(), sharedPreferences)
    }

    val componentRepository: ComponentRepository by lazy {
        ComponentRepository()
    }

    fun tag(companion: Any): String =
        companion::class.java.declaringClass?.canonicalName.toString().split(".").last()

    fun generateLogFunction(tag: String): (Any?) -> Int {
        return {
            Log.d(tag, it.toString())
        }
    }

    fun generateLogFunction(companion: Any): (Any?) -> Int {
        val t = tag(companion)
        return generateLogFunction(t)
    }
}
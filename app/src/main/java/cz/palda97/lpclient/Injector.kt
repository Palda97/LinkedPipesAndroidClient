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
        PipelineRepository(db.serverDao(), db.pipelineDao(), sharedPreferences)
    }
    val componentRepository: ComponentRepository by lazy {
        val db = AppDatabase.getInstance(context)
        val sharedPreferences = SharedPreferencesFactory.sharedPreferences(context)
        ComponentRepository(db.serverDao(), db.pipelineDao(), sharedPreferences)
    }
    val possibleComponentRepository: PossibleComponentRepository by lazy {
        val db = AppDatabase.getInstance(context)
        PossibleComponentRepository(db.serverDao(), db.pipelineDao())
    }
    val repositoryRoutines: RepositoryRoutines by lazy {
        RepositoryRoutines()
    }
    val executionDetailRepository: ExecutionDetailRepository by lazy {
        val db = AppDatabase.getInstance(context)
        ExecutionDetailRepository(db.executionDetailDao())
    }
    val executionNoveltyRepository: ExecutionNoveltyRepository by lazy {
        val db = AppDatabase.getInstance(context)
        ExecutionNoveltyRepository(db.serverDao(), db.executionNoveltyDao(), db.executionDao())
    }

    /**
     * @param companion Companion object
     * @return Last name of the companion's declaring class
     */
    fun tag(companion: Any): String =
        companion::class.java.declaringClass?.canonicalName.toString().split(".").last()

    //private val RELEASE: Boolean = !BuildConfig.DEBUG
    /**
     * If this is set to true, no logs are being printed.
     */
    const val RELEASE: Boolean = true
    private val logFun: (tag: String, msg: String) -> Int = if (RELEASE) {_, _ -> 0 } else Log::d

    /**
     * This generates function serving as an alternative to Log.d.
     * @param tag The first argument for the log function.
     * @return Log function if RELEASE is set to false. Otherwise it returns dummy function.
     */
    fun generateLogFunction(tag: String): (Any?) -> Int {
        return {
            logFun(tag, it.toString())
        }
    }

    /**
     * This generates function serving as an alternative to Log.d.
     * @param companion Companion object for generating tag, which will be used as the first
     * argument for log function
     * @return Log function if RELEASE is set to false. Otherwise it returns dummy function.
     */
    fun generateLogFunction(companion: Any): (Any?) -> Int {
        val t = tag(companion)
        return generateLogFunction(t)
    }
}
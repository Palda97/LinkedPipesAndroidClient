package cz.palda97.lpclient.model.services

import android.content.Context
import androidx.work.*
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.view.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus.Companion.isDone

/**
 * Class for monitoring executions of executed pipelines.
 */
class ExecutionMonitorPeriodic(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    /**
     * A monitoring round.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        AppInit.init(applicationContext)
        val repo = Injector.executionNoveltyRepository
        val executions = repo.downloadExecutions()
        repo.insertExecutions(executions)
        val noveltyWithExecutionList = repo.filterReallyNew(executions)
        val executionsForNotifications = noveltyWithExecutionList.mapNotNull {
            if (it.execution?.status.isDone)
                it.execution
            else
                null
        }
        executionsForNotifications.forEach {
            Notifications.executionNotification(applicationContext, it.pipelineName, it.status)
        }
        Result.success()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val UNIQUE_NAME = "ExecutionMonitorPeriodic"

        fun enqueue(context: Context): Operation {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<ExecutionMonitorPeriodic>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag("periodic")
                .build()
            return WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(UNIQUE_NAME)
        }
    }
}
package cz.palda97.lpclient.model.services

import android.content.Context
import androidx.work.*
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus.Companion.isDone
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.view.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Class for monitoring executions of executed pipelines.
 * @property params Data container containing execution id,
 * server id and pipeline name.
 */
class ExecutionMonitor(context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private lateinit var repo: ExecutionRepository

    /**
     * Monitor one execution for a minute.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        AppInit.init(applicationContext)
        val executionId = params.inputData.getString(EXECUTION_ID) ?: return@withContext Result.failure()
        val serverId = params.inputData.getLong(SERVER_ID, 0L)
        if (serverId == 0L) {
            return@withContext Result.failure()
        }
        val pipelineName = params.inputData.getString(PIPELINE_NAME) ?: return@withContext Result.failure()
        repo = Injector.executionRepository
        val status = monitorRoutine(serverId, executionId)
        if (status.isDone) {
            Notifications.executionNotification(applicationContext, pipelineName, status)
        }
        return@withContext Result.success()
    }

    private suspend fun monitorRoutine(
        serverId: Long,
        executionId: String
    ): ExecutionStatus? {
        for (i in 0..19) {
            delay(
                if (i < 10) 1000
                else 5000
            )
            l("monitorRoutine: round $i")
            val status = repo.fetchStatus(serverId, executionId)
            if (status == null || status.isDone)
                return status
        }
        return null
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        const val EXECUTION_ID = "EXECUTION_ID"
        const val SERVER_ID = "SERVER_ID"
        const val PIPELINE_NAME = "PIPELINE_NAME"

        /**
         * Create and enqueue request with the WorkManager.
         */
        fun enqueue(
            context: Context,
            executionId: String,
            serverId: Long,
            pipelineName: String
        ): Operation {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(
                EXECUTION_ID to executionId,
                SERVER_ID to serverId,
                PIPELINE_NAME to pipelineName
            )
            val request = OneTimeWorkRequestBuilder<ExecutionMonitor>()
                .setConstraints(constraints)
                .setInputData(data)
                .addTag("one time")
                .build()
            return WorkManager.getInstance(context)
                .enqueueUniqueWork(executionId, ExistingWorkPolicy.KEEP, request)
        }

        /*fun cancelWorker(context: Context, executionId: String) {
            l("cancelWorker $executionId")
            WorkManager.getInstance(context)
                .cancelUniqueWork(executionId)
        }*/
    }
}
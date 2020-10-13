package cz.palda97.lpclient.model.services

import android.content.Context
import android.util.Log
import androidx.work.*
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.view.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExecutionMonitor(context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return startMonitor()
    }

    private suspend fun startMonitor(): Result = withContext(Dispatchers.IO) {
        l("startMonitor thread: ${Thread.currentThread().name}")
        AppInit.init(applicationContext)
        val executionId =
            params.inputData.getString(EXECUTION_ID) ?: return@withContext Result.failure()
        val serverId = params.inputData.getLong(SERVER_ID, 0L)
        if (serverId == 0L) {
            return@withContext Result.failure()
        }
        val pipelineName =
            params.inputData.getString(PIPELINE_NAME) ?: return@withContext Result.failure()
        val repo = Injector.executionRepository
        l("monitor start $executionId")
        val status = repo.monitor(serverId, executionId)
        l("monitor done $executionId")
        Notifications.executionNotification(applicationContext, pipelineName, status)
        return@withContext Result.success()
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        const val EXECUTION_ID = "EXECUTION_ID"
        const val SERVER_ID = "SERVER_ID"
        const val PIPELINE_NAME = "PIPELINE_NAME"

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
                //.setInitialDelay(5, TimeUnit.SECONDS)
                //.addTag("lemonade")
                .build()
            return WorkManager.getInstance(context)
                //.enqueue(request)
                .enqueueUniqueWork(executionId, ExistingWorkPolicy.KEEP, request)
        }
    }
}
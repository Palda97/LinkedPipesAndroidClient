package cz.palda97.lpclient.model.services

import android.content.Context
import android.util.Log
import androidx.work.*
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExecutionMonitor(context: Context, private val params: WorkerParameters): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        AppInit.init(applicationContext)
        val executionId = params.inputData.getString(EXECUTION_ID) ?: return@withContext Result.failure()
        val serverId = params.inputData.getLong(SERVER_ID, 0L)
        if (serverId == 0L) {
            return@withContext Result.failure()
        }
        val repo = Injector.executionRepository
        l("monitor start $executionId")
        repo.monitor(serverId, executionId)
        l("monitor done $executionId")
        return@withContext Result.success()
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        const val EXECUTION_ID = "EXECUTION_ID"
        const val SERVER_ID = "SERVER_ID"

        fun enqueue(context: Context, executionId: String, serverId: Long): Operation {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(
                EXECUTION_ID to executionId,
                SERVER_ID to serverId
            )
            val request = OneTimeWorkRequestBuilder<ExecutionMonitor>()
                .setConstraints(constraints)
                .setInputData(data)
                .build()
            return WorkManager.getInstance(context)
                .enqueue(request)
        }
    }
}
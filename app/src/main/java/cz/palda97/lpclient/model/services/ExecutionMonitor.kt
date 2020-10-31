package cz.palda97.lpclient.model.services

import android.content.Context
import androidx.work.*
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.view.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ExecutionMonitor(context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private lateinit var repo: ExecutionRepository

    override suspend fun doWork(): Result {
        return monitor()
    }

    private suspend fun monitorRoutine(
        serverId: Long,
        executionId: String,
        mode: Int
    ): ExecutionStatus? = when (mode) {
        MODE_INIT -> {
            var status: ExecutionStatus? = null
            for (i in 0..19) {
                delay(
                    if (i < 10) 1000
                    else 5000
                )
                l("i = $i")
                status = repo.fetchStatus(serverId, executionId)
                l("status = ${status?.name ?: "null"}")
                if (status == null || status.isDone)
                    break
            }
            status
        }
        MODE_AFTER_MINUTE -> repo.fetchStatus(serverId, executionId)
        else -> null
    }

    private suspend fun monitor(): Result = withContext(Dispatchers.IO) {
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
        val mode = params.inputData.getInt(MODE, MODE_INIT)
        repo = Injector.executionRepository

        l("before monitorRoutine($serverId, $executionId, $mode)")
        val status = monitorRoutine(serverId, executionId, mode)

        when(status.isDone) {
            true -> {
                Notifications.executionNotification(applicationContext, pipelineName, status)
                return@withContext cancelWorker(applicationContext, executionId)
            }
            null -> {
                return@withContext cancelWorker(applicationContext, executionId)
            }
        }

        if (mode == MODE_INIT) {
            enqueue(applicationContext, executionId, serverId, pipelineName, true)
        }

        return@withContext Result.success()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        const val EXECUTION_ID = "EXECUTION_ID"
        const val SERVER_ID = "SERVER_ID"
        const val PIPELINE_NAME = "PIPELINE_NAME"

        fun enqueue(
            context: Context,
            executionId: String,
            serverId: Long,
            pipelineName: String,
            periodic: Boolean = false
        ): Operation {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(
                EXECUTION_ID to executionId,
                SERVER_ID to serverId,
                PIPELINE_NAME to pipelineName,
                MODE to if (periodic) MODE_AFTER_MINUTE else MODE_INIT
            )
            val request: Either<OneTimeWorkRequest, PeriodicWorkRequest> = when(periodic) {
                false -> Either.Left(OneTimeWorkRequestBuilder<ExecutionMonitor>()
                    .setConstraints(constraints)
                    .setInputData(data)
                    //.setInitialDelay(5, TimeUnit.SECONDS)
                    .addTag("one time")
                    .build())
                true -> Either.Right(PeriodicWorkRequestBuilder<ExecutionMonitor>(1, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .setInputData(data)
                    //.setInitialDelay(5, TimeUnit.SECONDS)
                    .addTag("periodic")
                    .build())
            }
            return when(request) {
                is Either.Left -> WorkManager.getInstance(context)
                    .enqueueUniqueWork(executionId, ExistingWorkPolicy.KEEP, request.value)
                is Either.Right -> WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(executionId, ExistingPeriodicWorkPolicy.REPLACE, request.value)
            }
        }

        private const val MODE = "MODE"

        private const val MODE_INIT = 0
        private const val MODE_AFTER_MINUTE = 1

        private fun cancelWorker(context: Context, executionId: String): Result {
            l("cancelWorker $executionId")
            WorkManager.getInstance(context)
                .cancelUniqueWork(executionId)
            return Result.success()
        }

        private val ExecutionStatus?.isDone: Boolean?
            get() = when (this) {
                null -> null
                else -> this.isDone
            }
        private val ExecutionStatus.isDone: Boolean
            get() = when (this) {
                ExecutionStatus.QUEUED -> false
                ExecutionStatus.RUNNING -> false
                else -> true
            }
    }
}
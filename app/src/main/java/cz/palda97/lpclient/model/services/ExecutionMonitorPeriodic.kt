package cz.palda97.lpclient.model.services

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.*
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.entities.execution.Execution.Companion.areDone
import cz.palda97.lpclient.view.Notifications
import cz.palda97.lpclient.view.Notifications.createForegroundInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Class for monitoring executions of executed pipelines.
 */
class ExecutionMonitorPeriodic(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        AppInit.init(applicationContext)
        val interval = inputData.getLong(DELAY, DELAY_NOT_SET)
        return@withContext if (interval == DELAY_NOT_SET) {
            periodicWork()
            Result.success()
        } else {
            foregroundWork(interval)
        }
    }

    private suspend fun periodicWork() {
        val repo = Injector.executionNoveltyRepository
        val executions = repo.cacheNovelties().areDone
        Notifications.executionNotification(executions)
    }

    private suspend fun foregroundWork(interval: Long): Result {
        setForeground(createForegroundInfo(interval))
        while (true) {
            delay(interval)
            periodicWork()
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val UNIQUE_NAME_PERIODIC = "ExecutionMonitorPeriodic_PERIODIC"
        private const val UNIQUE_NAME_ONE_SHOT = "ExecutionMonitorPeriodic_ONE_SHOT"

        const val LAST_INTERVAL = "EXECUTION_PERIODIC_MONITOR_LAST_INTERVAL"

        private val constraints
            get() = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        /**
         * Enqueue periodical execution updates for notification purposes.
         * If repeat interval is less than 15 minutes,
         * updates will be mediated through foreground service.
         * Already running updates will be replaced only if repeat interval has changed.
         */
        @SuppressLint("ApplySharedPref")
        fun enqueue(context: Context, repeatInterval: Long, timeUnit: TimeUnit): Operation {
            require(repeatInterval > 0) { "RepeatInterval must be bigger than 0, but is $repeatInterval." }
            val intervalSeconds = timeUnit.toSeconds(repeatInterval)
            val sharedPreferences = SharedPreferencesFactory.sharedPreferences(context)
            val lastInterval = sharedPreferences.getLong(LAST_INTERVAL, 0L)
            val hasIntervalChanged = intervalSeconds != lastInterval
            if (hasIntervalChanged) {
                sharedPreferences.edit().putLong(LAST_INTERVAL, intervalSeconds).commit()
            }
            return if (timeUnit.toMinutes(repeatInterval) < 15)
                enqueueForeground(context, repeatInterval, timeUnit, hasIntervalChanged)
            else
                enqueuePeriodic(context, repeatInterval, timeUnit, hasIntervalChanged)
        }

        private const val DELAY = "DELAY"
        private const val DELAY_NOT_SET = -1L

        private fun enqueueForeground(context: Context, repeatInterval: Long, timeUnit: TimeUnit, hasIntervalChanged: Boolean): Operation {
            val policy = if (hasIntervalChanged)
                ExistingWorkPolicy.REPLACE
            else
                ExistingWorkPolicy.KEEP
            val data = workDataOf(
                DELAY to timeUnit.toMillis(repeatInterval)
            )
            val request = OneTimeWorkRequestBuilder<ExecutionMonitorPeriodic>()
                .setConstraints(constraints)
                .setInputData(data)
                .addTag("foreground")
                .build()
            return WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_NAME_PERIODIC, policy, request)
        }

        /**
         * Enqueue periodic execution monitor.
         * If monitor already exists, it will be replaced only if the repeat interval has changed.
         * @param repeatInterval can not be less than 15 minutes.
         */
        private fun enqueuePeriodic(context: Context, repeatInterval: Long, timeUnit: TimeUnit, hasIntervalChanged: Boolean): Operation {
            val intervalMinutes = timeUnit.toMinutes(repeatInterval)
            require(intervalMinutes >= 15) { "RepeatInterval can not be less than 15 minutes, but was $intervalMinutes minutes" }
            val policy = if (hasIntervalChanged)
                ExistingPeriodicWorkPolicy.REPLACE
            else
                ExistingPeriodicWorkPolicy.KEEP
            val request = PeriodicWorkRequestBuilder<ExecutionMonitorPeriodic>(repeatInterval, timeUnit)
                .setConstraints(constraints)
                .addTag("periodic")
                .build()
            return WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME_PERIODIC, policy, request)
        }

        /**
         * Enqueue one shot execution monitor.
         * If monitor already exists, it will be not be enqueued.
         */
        fun enqueueOneShot(context: Context): Operation {
            val request = OneTimeWorkRequestBuilder<ExecutionMonitorPeriodic>()
                .setConstraints(constraints)
                .addTag("one shot")
                .build()
            return WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_NAME_ONE_SHOT, ExistingWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(UNIQUE_NAME_PERIODIC)
        }
    }
}
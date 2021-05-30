package cz.palda97.lpclient.view

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.services.NotificationBroadcastReceiver
import cz.palda97.lpclient.viewmodel.executions.resource
import java.util.concurrent.TimeUnit

/**
 * Class for working with notifications.
 */
object Notifications {

    private const val NOTIFICATION_ID = "NOTIFICATION_ID"
    const val NOTIFICATIONS = "NOTIFICATIONS"
    const val NOTIFICATIONS_CANCEL = "NOTIFICATIONS_CANCEL"

    /**
     * Check settings if notifications are turned on.
     */
    private fun allowNotifications(context: Context): Boolean {
        val sharedPreferences = SharedPreferencesFactory.sharedPreferences(context)
        return sharedPreferences.getBoolean(NOTIFICATIONS, false)
    }

    private fun ExecutionStatus?.title(context: Context): String = context.getString(
        when (this) {
            ExecutionStatus.FINISHED -> R.string.execution_notification_finished
            ExecutionStatus.FAILED -> R.string.execution_notification_failed
            ExecutionStatus.RUNNING -> R.string.execution_notification_running
            ExecutionStatus.CANCELLED -> R.string.execution_notification_cancelled
            ExecutionStatus.DANGLING -> R.string.execution_notification_dangling
            ExecutionStatus.CANCELLING -> R.string.execution_notification_cancelling
            ExecutionStatus.QUEUED -> R.string.execution_notification_queued
            ExecutionStatus.MAPPED -> R.string.execution_notification_mapped
            null -> R.string.execution_notification_null_status
        }
    )

    /**
     * Display a notification about pipeline execution result if notifications are turned on.
     */
    fun executionNotification(executions: List<Execution>) = executions.map {
        executionNotification(Injector.context, it.pipelineName, it.status)
    }

    /**
     * Display a notification about pipeline execution result if notifications are turned on.
     */
    fun executionNotification(context: Context, text: String, status: ExecutionStatus?) {
        if (!allowNotifications(context))
            return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, AppInit.CHANNEL_ID)
            //.setSmallIcon(R.drawable.ic_baseline_refresh_24)
            .setSmallIcon(R.mipmap.etl_icon_foreground)
            .setColor(ContextCompat.getColor(context, R.color.brand_orange))
            //.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.etl_icon_foreground))
            //.setContentTitle(context.getString(R.string.execution_notification_title))
            .setContentTitle(status.title(context))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setDefaults(Notification.DEFAULT_SOUND)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                status?.let {
                    setLargeIcon(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            it.resource,
                            null
                        )?.toBitmap()
                    )
                }
            }

        val notificationId = getNotificationId(context)
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private const val NOTIFICATION_ID_MIN = 10

    // NEVER GIVE NOTIFICATION ID = 0 !!!
    private const val NOTIFICATION_ID_MIN_FOREGROUND = 1

    private fun getNotificationId(context: Context) = getNotificationId(SharedPreferencesFactory.sharedPreferences(context))
    private fun getNotificationId(sharedPreferences: SharedPreferences): Int {
        val id = sharedPreferences.getInt(NOTIFICATION_ID, NOTIFICATION_ID_MIN)
        val tmp = id + 1
        val newId = if (tmp < NOTIFICATION_ID_MIN) NOTIFICATION_ID_MIN else tmp
        sharedPreferences.edit().putInt(NOTIFICATION_ID, newId).apply()
        return id
    }

    fun ListenableWorker.createForegroundInfo(delay: Long): ForegroundInfo {
        val unit = TimeUnit.MILLISECONDS
        val minutes = TimeUnit.MILLISECONDS.toMinutes(delay)
        val time = if (minutes < 1) unit.toSeconds(delay) to applicationContext.getString(R.string.seconds) else minutes to applicationContext.getString(R.string.minutes)
        val progress = "${applicationContext.getString(R.string.monitor_with_frequency_around)} ${time.first} ${time.second}"
        return createForegroundInfo(progress)
    }
    private fun ListenableWorker.createForegroundInfo(progress: String): ForegroundInfo {
        val title = applicationContext.getString(R.string.execution_monitor)
        val cancel = applicationContext.getString(R.string.cancel)
        /*val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)*/
        val cancelIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, NotificationBroadcastReceiver::class.java),
            0
        )
        val notification = NotificationCompat.Builder(applicationContext, AppInit.CHANNEL_ID_FOREGROUND)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.mipmap.etl_icon_foreground)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, cancelIntent)
            .build()
        return ForegroundInfo(NOTIFICATION_ID_MIN_FOREGROUND, notification)
    }
}
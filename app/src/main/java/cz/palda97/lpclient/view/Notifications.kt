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
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.R
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.viewmodel.executions.resource

/**
 * Class for working with notifications.
 */
object Notifications {

    private const val NOTIFICATION_ID = "NOTIFICATION_ID"
    const val NOTIFICATIONS = "NOTIFICATIONS"

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
            null -> R.string.execution_notification_null_status
        }
    )

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

        with(NotificationManagerCompat.from(context)) {
            notify(getNotificationId(context), builder.build())
        }
    }

    private fun getNotificationId(context: Context) = getNotificationId(SharedPreferencesFactory.sharedPreferences(context))
    private fun getNotificationId(sharedPreferences: SharedPreferences): Int {
        val id = sharedPreferences.getInt(NOTIFICATION_ID, 0)
        val tmp = id + 1
        val newId = if (tmp < 0) 0 else tmp
        sharedPreferences.edit().putInt(NOTIFICATION_ID, newId).apply()
        return id
    }
}
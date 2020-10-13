package cz.palda97.lpclient.view

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.R
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel


object Notifications {

    private const val NOTIFICATION_ID = 420
    const val NOTIFICATIONS = "NOTIFICATIONS"

    private fun allowNotifications(context: Context): Boolean {
        val sharedPreferences = SharedPreferencesFactory.sharedPreferences(context)
        return sharedPreferences.getBoolean(NOTIFICATIONS, false)
    }

    fun executionNotification(context: Context, text: String) {
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
            .setContentTitle(context.getString(R.string.execution_notification_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setDefaults(Notification.DEFAULT_SOUND)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //startForeground(NOTIFICATION_ID, builder.build())
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    /*fun makeExecutionNotification(context: Context, text: String) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, AppInit.CHANNEL_ID)
            //.setSmallIcon(R.drawable.ic_baseline_refresh_24)
            .setSmallIcon(R.mipmap.etl_icon_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.etl_icon_foreground))
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //startForeground(NOTIFICATION_ID, builder.build())
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }*/

    /*fun makeExecutionNotification(context: Context, smallText: String, bigText: String) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, AppInit.CHANNEL_ID)
            //.setSmallIcon(R.drawable.ic_baseline_refresh_24)
            .setSmallIcon(R.mipmap.etl_icon_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.etl_icon_foreground))
            .setContentTitle(context.getString(R.string.execution_notification_title))
            .setContentText(smallText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //startForeground(NOTIFICATION_ID, builder.build())
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }*/
}
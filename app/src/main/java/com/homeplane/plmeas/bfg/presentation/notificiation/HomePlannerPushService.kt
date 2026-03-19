package com.homeplane.plmeas.bfg.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.homeplane.plmeas.HomePlannerActivity
import com.homeplane.plmeas.R
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication

private const val HOME_PLANNER_CHANNEL_ID = "home_planner_notifications"
private const val HOME_PLANNER_CHANNEL_NAME = "HomePlanner Notifications"
private const val HOME_PLANNER_NOT_TAG = "HomePlanner"

class HomePlannerPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                homePlannerShowNotification(it.title ?: HOME_PLANNER_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                homePlannerShowNotification(it.title ?: HOME_PLANNER_NOT_TAG, it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            homePlannerHandleDataPayload(remoteMessage.data)
        }
    }

    private fun homePlannerShowNotification(title: String, message: String, data: String?) {
        val homePlannerNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HOME_PLANNER_CHANNEL_ID,
                HOME_PLANNER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            homePlannerNotificationManager.createNotificationChannel(channel)
        }

        val homePlannerIntent = Intent(this, HomePlannerActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val homePlannerPendingIntent = PendingIntent.getActivity(
            this,
            0,
            homePlannerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val homePlannerNotification = NotificationCompat.Builder(this, HOME_PLANNER_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.home_planner_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(homePlannerPendingIntent)
            .build()

        homePlannerNotificationManager.notify(System.currentTimeMillis().toInt(), homePlannerNotification)
    }

    private fun homePlannerHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}
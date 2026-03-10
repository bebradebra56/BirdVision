package com.vision.birdvisionpr.verfs.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vision.birdvisionpr.BirdVisionActivity
import com.vision.birdvisionpr.R

private const val BIRD_VISION_CHANNEL_ID = "bird_vision_notifications"
private const val BIRD_VISION_CHANNEL_NAME = "BirdVision Notifications"
private const val BIRD_VISION_NOT_TAG = "BirdVision"

class BirdVisionPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                birdVisionShowNotification(it.title ?: BIRD_VISION_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                birdVisionShowNotification(it.title ?: BIRD_VISION_NOT_TAG, it.body ?: "", data = null)
            }
        }

    }

    private fun birdVisionShowNotification(title: String, message: String, data: String?) {
        val birdVisionNotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                BIRD_VISION_CHANNEL_ID,
                BIRD_VISION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            birdVisionNotificationManager.createNotificationChannel(channel)
        }

        val birdVisionIntent = Intent(this, BirdVisionActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val birdVisionPendingIntent = PendingIntent.getActivity(
            this,
            0,
            birdVisionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val birdVisionNotification = NotificationCompat.Builder(this, BIRD_VISION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.bird_vision_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(birdVisionPendingIntent)
            .build()

        birdVisionNotificationManager.notify(System.currentTimeMillis().toInt(), birdVisionNotification)
    }

}
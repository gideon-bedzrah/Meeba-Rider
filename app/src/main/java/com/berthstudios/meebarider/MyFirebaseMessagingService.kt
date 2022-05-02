package com.berthstudios.meebarider

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val CHANNEL_ID = "HEADS_UP_NOTIFICATION"

        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "heads up notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
       val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.box_24x24__1x)
            .setAutoCancel(true)
           .setDefaults(Notification.DEFAULT_ALL)
           .setVisibility(Notification.VISIBILITY_PUBLIC)
           .setPriority(Notification.PRIORITY_MAX)
        NotificationManagerCompat.from(this).notify(1,notification.build())
    }
}
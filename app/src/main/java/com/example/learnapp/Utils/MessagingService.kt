package com.example.learnapp.Utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.learnapp.R
import com.example.learnapp.View.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// Kế thừa FirebaseMessagingService để nhận thông báo
class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Kiểm tra xem code có thực sự chạy vào đây không
        android.util.Log.d("DEBUG_NOTI", "------> ĐÃ NHẬN TÍN HIỆU TỪ ADMIN <------")

        // Trong MessagingService.kt
        val prefs = getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
        val isSaved = prefs.edit().putBoolean("has_new_notification", true).commit() // Dùng commit()

        android.util.Log.d("DEBUG_NOTI", "---- NHẬN TIN TỪ ADMIN WEB ----")
        android.util.Log.d("DEBUG_NOTI", "Lưu thành công: $isSaved")


        val title = remoteMessage.data["title"] ?: "LangGo"
        val body = remoteMessage.data["body"] ?: "Thông báo mới"
        showNotification(title, body)
    }

    // Trong MessagingService.kt
    private fun showNotification(title: String, message: String) {
        val channelId = "learn_app_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Học tập",
                NotificationManager.IMPORTANCE_HIGH // BẮT BUỘC HIGH để hiện popup
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.logoapp)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logoremove)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
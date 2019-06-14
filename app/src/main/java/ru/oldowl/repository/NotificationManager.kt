package ru.oldowl.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import ru.oldowl.R
import ru.oldowl.ui.MainActivity


class NotificationManager(
        private val context: Context
) {

    companion object {
        private const val GENERAL_CHANNEL_ID = "general_channel"

        private const val NOTIFICATION_ID = 128
    }

    private val notificationService by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        createChannel()
    }

    fun showDownloadingArticle() {
        TODO("Not implemented")
    }

    fun showNewArticles(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_new_article_title, count))
                .setContentText(context.getString(R.string.notification_new_article_text))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .build()

        notificationService.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.notification_general_channel_name)
            val channel = NotificationChannel(GENERAL_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW)

            notificationService.createNotificationChannel(channel)
        }
    }
}
package com.calendar.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calendar.R

/**
 * 闹钟接收器，用于接收闹钟触发事件并显示通知
 */
class AlarmReceiver : BroadcastReceiver() {

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // 创建通知渠道（Android 8.0+）
        createNotificationChannel(context)

        // 从Intent中获取日程信息
        val scheduleId = intent.getLongExtra("SCHEDULE_ID", 0)
        val title = intent.getStringExtra("SCHEDULE_TITLE") ?: "日程提醒"
        val description = intent.getStringExtra("SCHEDULE_DESCRIPTION") ?: ""

        // 构建通知
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 使用启动器图标
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // 显示通知
        with(NotificationManagerCompat.from(context))  {
            notify(scheduleId.toInt(), builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Android 8.0+ 需要创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "日程提醒"
            val descriptionText = "用于接收日程提醒通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // 注册通知渠道
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "calendar_notification_channel"
    }
}
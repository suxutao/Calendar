package com.calendar.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calendar.MainActivity
import com.calendar.R
import com.calendar.db.AppDatabase
import com.calendar.model.Schedule

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SHOW_REMINDER = "com.calendar.receiver.ACTION_SHOW_REMINDER"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val CHANNEL_ID = "calendar_reminder_channel"
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_SHOW_REMINDER) return

        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1)
        if (scheduleId == -1L) return

        showNotification(context, scheduleId)
    }

    private fun showNotification(context: Context, scheduleId: Long) {
        createNotificationChannel(context)

        try {
            val database = AppDatabase.getInstance(context)
            val schedule = database.scheduleDao().getScheduleByIdSync(scheduleId) ?: return

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = "日程提醒: ${schedule.title}"
            val description = schedule.description ?: "您有一个日程待处理"

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            NotificationManagerCompat.from(context).notify(scheduleId.toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "日程提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于接收日程提醒通知"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

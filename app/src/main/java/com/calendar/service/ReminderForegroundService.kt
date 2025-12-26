package com.calendar.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calendar.MainActivity
import com.calendar.R
import com.calendar.db.AppDatabase
import com.calendar.model.ReminderType
import com.calendar.model.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ReminderForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var checkJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        startCheckingReminders()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        checkJob?.cancel()
        serviceScope.cancel()
    }

    private fun startCheckingReminders() {
        checkJob?.cancel()
        checkJob = serviceScope.launch {
            while (isActive) {
                checkAndNotifyReminders()
                delay(CHECK_INTERVAL)
            }
        }
    }

    private suspend fun checkAndNotifyReminders() {
        try {
            val database = AppDatabase.getInstance(this)
            val schedules = database.scheduleDao().getAllSchedulesSync()
            val currentTime = System.currentTimeMillis()
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            schedules.forEach { schedule ->
                if (schedule.reminderType != ReminderType.NONE) {
                    val reminderTime = calculateReminderTime(schedule)
                    val notifyKey = "notified_${schedule.id}"
                    val hasNotified = prefs.getBoolean(notifyKey, false)

                    when {
                        currentTime >= reminderTime && currentTime < reminderTime + NOTIFICATION_WINDOW && !hasNotified -> {
                            showReminderNotification(schedule)
                            prefs.edit().putBoolean(notifyKey, true).apply()
                        }
                        currentTime < reminderTime - NOTIFICATION_WINDOW -> {
                            prefs.edit().putBoolean(notifyKey, false).apply()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking reminders", e)
        }
    }

    private fun calculateReminderTime(schedule: Schedule): Long {
        val reminderMinutes = schedule.reminderType.minutes
        return schedule.startTime - (reminderMinutes * MINUTE_IN_MILLIS)
    }

    private fun showReminderNotification(schedule: Schedule) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("日程提醒: ${schedule.title}")
            .setContentText(schedule.description ?: "您有一个日程待处理")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(this).notify(schedule.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "日程提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于接收日程提醒通知"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("日历服务运行中")
            .setContentText("正在监控日程提醒")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val TAG = "ReminderForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "calendar_reminder_channel"
        private const val CHECK_INTERVAL = 30_000L
        private const val NOTIFICATION_WINDOW = 60_000L
        private const val PREFS_NAME = "reminder_prefs"
        private const val EXTRA_SCHEDULE_ID = "schedule_id"
        private const val MINUTE_IN_MILLIS = 60 * 1000L

        fun start(context: Context) {
            val intent = Intent(context, ReminderForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ReminderForegroundService::class.java)
            context.stopService(intent)
        }
    }
}

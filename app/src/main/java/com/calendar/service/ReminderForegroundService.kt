package com.calendar.service

import android.Manifest
import android.app.Notification
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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calendar.MainActivity
import com.calendar.R
import com.calendar.db.AppDatabase
import com.calendar.model.ReminderType
import com.calendar.model.Schedule
import com.calendar.util.LunarCalendarUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.core.content.edit

class ReminderForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var checkJob: Job? = null
    @Volatile
    private var database: AppDatabase? = null

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            database = AppDatabase.getInstance(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val notification = createForegroundNotification()
            if (notification != null) {
                startForeground(NOTIFICATION_ID, notification)
            }

            if (intent?.action == ACTION_CHECK_NOW) {
                Log.d(TAG, "Received ACTION_CHECK_NOW, triggering immediate check")
                serviceScope.launch {
                    checkAndNotifyReminders()
                }
            } else {
                startCheckingReminders()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        checkJob?.cancel()
        checkJob = null
        serviceScope.cancel()
        database = null
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCheckingReminders() {
        try {
            checkJob?.cancel()
            checkJob = serviceScope.launch {
                Log.i(TAG, "Reminder check loop started")
                while (isActive) {
                    try {
                        checkAndNotifyReminders()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in check loop iteration", e)
                    }
                    delay(CHECK_INTERVAL)
                }
                Log.i(TAG, "Reminder check loop ended")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting reminder check", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun checkAndNotifyReminders() {
        val db = database ?: run {
            Log.w(TAG, "Database is null, skipping reminder check")
            return
        }
        try {
            val schedules = db.scheduleDao().getAllSchedulesSync()
            Log.d(TAG, "Found ${schedules.size} schedules")

            if (schedules.isEmpty()) {
                Log.d(TAG, "No schedules to check")
                return
            }

            val currentTime = System.currentTimeMillis()
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val reminderServiceEnabled = prefs.getBoolean("reminder_service_enabled", true)
            Log.d(TAG, "Reminder service enabled: $reminderServiceEnabled")

            if (!reminderServiceEnabled) {
                Log.d(TAG, "Reminder service is disabled, skipping all notifications")
                return
            }

            var notificationsSent = 0

            schedules.forEach { schedule ->
                if (schedule == null) return@forEach
                if (schedule.reminderType == ReminderType.NONE) {
                    Log.d(TAG, "Schedule '${schedule.title}' has no reminder (NONE), skipping")
                    return@forEach
                }

                val reminderTime = calculateReminderTime(schedule)
                val notifyKey = "notified_${schedule.id}"
                val hasNotified = prefs.getBoolean(notifyKey, false)

                val missedWindow = 5 * 60 * 1000L

                Log.d(TAG, "=== Checking schedule: ${schedule.title} ===")
                Log.d(TAG, "  ID: ${schedule.id}")
                Log.d(TAG, "  isAllDay: ${schedule.isAllDay}")
                Log.d(TAG, "  reminderType: ${schedule.reminderType}")
                Log.d(TAG, "  reminderType.minutes: ${schedule.reminderType.minutes}")
                Log.d(TAG, "  startTime: ${schedule.startTime} (${Instant.ofEpochMilli(schedule.startTime).atZone(ZoneId.systemDefault()).toLocalDateTime()})")
                Log.d(TAG, "  reminderTime: $reminderTime (${if (reminderTime > 0) Instant.ofEpochMilli(reminderTime).atZone(
                    ZoneId.systemDefault()).toLocalDateTime() else "N/A"})")
                Log.d(TAG, "  currentTime: $currentTime (${Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault()).toLocalDateTime()})")
                Log.d(TAG, "  hasNotified: $hasNotified")
                Log.d(TAG, "  missedWindow: $missedWindow ms (${missedWindow / 60000} minutes)")
                Log.d(TAG, "  in window: ${currentTime >= reminderTime && currentTime < reminderTime + missedWindow}")
                Log.d(TAG, "  condition met: ${currentTime >= reminderTime && currentTime < reminderTime + missedWindow && !hasNotified}")

                when {
                    currentTime >= reminderTime && currentTime < reminderTime + missedWindow && !hasNotified -> {
                        Log.i(TAG, ">>> SHOWING NOTIFICATION for schedule: ${schedule.title} <<<")
                        showReminderNotification(schedule)
                        prefs.edit().putBoolean(notifyKey, true).apply()
                        notificationsSent++
                    }
                    currentTime < reminderTime - missedWindow -> {
                        Log.d(TAG, "  Resetting notification flag for future reminder")
                        prefs.edit { putBoolean(notifyKey, false) }
                    }
                    hasNotified -> {
                        Log.d(TAG, "  Already notified, skipping")
                    }
                    currentTime >= reminderTime + missedWindow -> {
                        Log.d(TAG, "  Missed the notification window")
                    }
                    currentTime < reminderTime -> {
                        Log.d(TAG, "  Reminder time not reached yet")
                    }
                }
            }

            Log.d(TAG, "Check complete. Total schedules: ${schedules.size}, Notifications sent this round: $notificationsSent")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking reminders", e)
        }
    }

    private fun calculateReminderTime(schedule: Schedule): Long {
        return when (schedule.reminderType) {
            ReminderType.NONE -> 0L
            ReminderType.AT_START -> schedule.startTime
            else -> {
                val minutes = schedule.reminderType.minutes
                if (schedule.isAllDay) {
                    when {
                        minutes < 0 -> {
                            schedule.startTime + minutes * MINUTE_IN_MILLIS
                        }
                        minutes >= 360 -> {
                            schedule.startTime + (minutes - DAY_IN_MINUTES) * MINUTE_IN_MILLIS
                        }
                        else -> schedule.startTime - (minutes * MINUTE_IN_MILLIS)
                    }
                } else {
                    schedule.startTime - (minutes * MINUTE_IN_MILLIS)
                }
            }
        }
    }

    private fun showReminderNotification(schedule: Schedule) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            Log.d(TAG, "Notification permission status: $permissionCheck")
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted, skipping notification")
                return
            }
        }

        try {
            Log.d(TAG, "Creating notification for schedule: ${schedule.title}")
            
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

            val title = "日程提醒: ${schedule.title}"
            val description = schedule.description ?: "您有一个日程待处理"

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notification = builder.build()
            Log.d(TAG, "Notification built successfully, notifying with id: ${schedule.id.toInt()}")
            
            NotificationManagerCompat.from(this).notify(schedule.id.toInt(), notification)
            Log.i(TAG, "Notification sent successfully for: ${schedule.title}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show notification - permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }

    private fun createNotificationChannel() {
        createNotificationChannel(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createForegroundNotification(): Notification? {
        return try {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val today = LocalDate.now()
            val weekFormatter = DateTimeFormatter.ofPattern("EEEE")
            val lunarInfo = LunarCalendarUtil.formatLunarDate(today)
            val dateInfo = "${today.monthValue}月${today.dayOfMonth}日 ${today.format(weekFormatter)} $lunarInfo"

            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(dateInfo)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setSilent(true)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating foreground notification", e)
            null
        }
    }

    companion object {
        private const val TAG = "ReminderForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val TEST_NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "calendar_reminder_channel"
        private const val CHECK_INTERVAL = 30_000L
        private const val PREFS_NAME = "reminder_prefs"
        private const val EXTRA_SCHEDULE_ID = "schedule_id"
        private const val MINUTE_IN_MILLIS = 60 * 1000L
        private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val DAY_IN_MINUTES = 24 * 60
        const val ACTION_CHECK_NOW = "com.calendar.service.CHECK_NOW"

        fun start(context: Context) {
            try {
                Log.i(TAG, "Starting ReminderForegroundService")
                val intent = Intent(context, ReminderForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d(TAG, "Starting as foreground service (Android 8+)")
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.i(TAG, "ReminderForegroundService started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground service", e)
            }
        }

        fun stop(context: Context) {
            try {
                Log.i(TAG, "Stopping ReminderForegroundService")
                val intent = Intent(context, ReminderForegroundService::class.java)
                context.stopService(intent)
                Log.i(TAG, "ReminderForegroundService stopped successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping foreground service", e)
            }
        }

        fun triggerCheckNow(context: Context) {
            try {
                Log.i(TAG, "Triggering immediate reminder check")
                val intent = Intent(context, ReminderForegroundService::class.java).apply {
                    action = ACTION_CHECK_NOW
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering immediate check", e)
            }
        }

        fun sendTestNotification(context: Context) {
            try {
                Log.i(TAG, "Sending test notification")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    Log.d(TAG, "Test notification permission status: $permissionCheck")
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "Notification permission not granted, cannot send test notification")
                        return
                    }
                }

                createNotificationChannel(context)

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("测试通知")
                    .setContentText("这是测试通知，如果能收到说明通知系统正常！")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID, builder.build())
                Log.i(TAG, "Test notification sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending test notification", e)
            }
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "日程提醒",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "用于接收日程提醒通知"
                    }
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating notification channel", e)
                }
            }
        }
    }
}

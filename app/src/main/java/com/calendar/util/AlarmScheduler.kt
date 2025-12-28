package com.calendar.util

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.calendar.model.ReminderType
import com.calendar.model.Schedule
import com.calendar.receiver.AlarmReceiver

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"
    private const val MINUTE_IN_MILLIS = 60 * 1000L
    private const val DAY_IN_MINUTES = 24 * 60

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleReminder(context: Context, schedule: Schedule) {
        if (schedule.reminderType == ReminderType.NONE) {
            Log.d(TAG, "Schedule '${schedule.title}' has no reminder, skipping")
            return
        }

        val reminderTime = calculateReminderTime(schedule)
        if (reminderTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Reminder time has already passed for '${schedule.title}', skipping")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SHOW_REMINDER
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
            Log.i(TAG, "Scheduled alarm for '${schedule.title}' at $reminderTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm, falling back to inexact", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for '${schedule.title}'", e)
        }
    }

    fun cancelReminder(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SHOW_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i(TAG, "Cancelled alarm for schedule ID: $scheduleId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm for schedule ID: $scheduleId", e)
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun rescheduleReminder(context: Context, schedule: Schedule) {
        cancelReminder(context, schedule.id)
        scheduleReminder(context, schedule)
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
}

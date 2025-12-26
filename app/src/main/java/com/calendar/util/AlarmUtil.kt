package com.calendar.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.calendar.db.AppDatabase
import com.calendar.model.ReminderType
import com.calendar.model.Schedule
import com.calendar.receiver.AlarmReceiver
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 闹钟工具类，用于设置和管理日程提醒闹钟
 */
@RequiresApi(Build.VERSION_CODES.O)
object AlarmUtil {
    
    /**
     * 为日程设置闹钟
     */
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun setAlarm(context: Context, schedule: Schedule) {
        // 如果提醒类型为NONE，则不设置闹钟
        if (schedule.reminderType == ReminderType.NONE) {
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("SCHEDULE_TITLE", schedule.title)
            putExtra("SCHEDULE_DESCRIPTION", schedule.description)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 计算提醒时间
        val reminderTime = calculateReminderTime(schedule)
        
        // 设置闹钟
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )
    }
    
    /**
     * 取消日程的闹钟
     */
    fun cancelAlarm(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    /**
     * 重置所有闹钟
     */
    @SuppressLint("ScheduleExactAlarm")
    fun resetAllAlarms(context: Context) {
        // 获取所有日程
        val database = AppDatabase.getInstance(context)
        val schedules = database.scheduleDao().getAllSchedulesSync()
        
        // 为每个日程重新设置闹钟
        schedules.forEach { schedule ->
            setAlarm(context, schedule)
        }
    }
    
    /**
     * 计算提醒时间
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateReminderTime(schedule: Schedule): Long {
        val startTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(schedule.startTime),
            ZoneId.systemDefault()
        )
        
        // 根据提醒类型计算提前时间
        val reminderMinutes = when (schedule.reminderType) {
            com.calendar.model.ReminderType.NONE -> 0
            com.calendar.model.ReminderType.FIVE_MINUTES -> 5
            com.calendar.model.ReminderType.TEN_MINUTES -> 10
            com.calendar.model.ReminderType.THIRTY_MINUTES -> 30
            com.calendar.model.ReminderType.ONE_HOUR -> 60
        }
        
        return startTime.minusMinutes(reminderMinutes.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
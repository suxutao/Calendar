package com.calendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.calendar.db.AppDatabase
import com.calendar.service.ReminderForegroundService
import com.calendar.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, ReminderForegroundService::class.java))
            } else {
                context.startService(Intent(context, ReminderForegroundService::class.java))
            }

            CoroutineScope(Dispatchers.IO).launch {
                restoreAllReminders(context)
            }
        }
    }

    private suspend fun restoreAllReminders(context: Context) {
        try {
            val database = AppDatabase.getInstance(context)
            val schedules = database.scheduleDao().getAllSchedulesSync()

            schedules.filterNotNull().forEach { schedule ->
                if (schedule.reminderType != com.calendar.model.ReminderType.NONE) {
                    AlarmScheduler.scheduleReminder(context, schedule)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

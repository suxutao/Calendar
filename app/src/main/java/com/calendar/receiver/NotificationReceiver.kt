package com.calendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.calendar.util.AlarmUtil

/**
 * 通知接收器，用于处理系统广播事件（如开机完成）
 */
class NotificationReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // 处理开机完成事件，重新设置所有闹钟
            Intent.ACTION_BOOT_COMPLETED -> {
                AlarmUtil.resetAllAlarms(context)
            }
            // 处理时间变化事件
            Intent.ACTION_TIME_CHANGED -> {
                AlarmUtil.resetAllAlarms(context)
            }
            // 处理时区变化事件
            Intent.ACTION_TIMEZONE_CHANGED -> {
                AlarmUtil.resetAllAlarms(context)
            }
        }
    }
}
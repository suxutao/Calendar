package com.calendar.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calendar.model.ReminderType

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val startTime: Long, // 开始时间戳（毫秒）
    val endTime: Long,   // 结束时间戳（毫秒）
    val reminderType: ReminderType = ReminderType.TEN_MINUTES, // 兼容新枚举值
    val isAllDay: Boolean = false,
    val createTime: Long = System.currentTimeMillis() // 创建时间
)
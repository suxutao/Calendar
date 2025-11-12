package com.calendar.db

import androidx.room.TypeConverter
import com.calendar.model.ReminderType

class Converters {
    // 枚举转字符串（新增枚举值会自动以name存储）
    @TypeConverter
    fun toReminderType(value: String): ReminderType = ReminderType.valueOf(value)

    // 字符串转枚举（新增枚举值可通过name解析）
    @TypeConverter
    fun fromReminderType(type: ReminderType): String = type.name
}
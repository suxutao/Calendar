package com.calendar.db

import androidx.room.TypeConverter
import com.calendar.model.ReminderType

class Converters {
    @TypeConverter
    fun toReminderType(value: String): ReminderType {
        return try {
            ReminderType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ReminderType.NONE
        }
    }

    @TypeConverter
    fun fromReminderType(type: ReminderType): String = type.name
}
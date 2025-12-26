package com.calendar.model

enum class ReminderType(val minutes: Int, val displayName: String) {
    NONE(0, "不提醒"),
    FIVE_MINUTES(5, "5分钟前"),
    TEN_MINUTES(10, "10分钟前"),
    THIRTY_MINUTES(30, "30分钟前"),
    ONE_HOUR(60, "1小时前");

    companion object {
        fun fromMinutes(minutes: Int): ReminderType {
            return entries.find { it.minutes == minutes } ?: NONE
        }
    }
}

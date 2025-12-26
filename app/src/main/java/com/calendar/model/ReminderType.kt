package com.calendar.model

enum class ReminderType(val minutes: Int, val displayName: String, val isAllDayOnly: Boolean = false) {
    NONE(0, "不提醒"),
    AT_START(0, "日程开始时"),
    FIVE_MINUTES(5, "5分钟前"),
    TEN_MINUTES(10, "10分钟前"),
    THIRTY_MINUTES(30, "30分钟前"),
    ONE_HOUR(60, "1小时前"),
    ALL_DAY_PREV_NIGHT_8PM(-20, "前一天晚上8点", true),
    ALL_DAY_PREV_NIGHT_9PM(-21, "前一天晚上9点", true),
    ALL_DAY_PREV_NIGHT_10PM(-22, "前一天晚上10点", true),
    ALL_DAY_MORNING_6AM(360, "当天早上6点", true),
    ALL_DAY_MORNING_7AM(420, "当天早上7点", true),
    ALL_DAY_MORNING_8AM(480, "当天早上8点", true);

    companion object {
        fun fromMinutes(minutes: Int): ReminderType {
            return entries.find { it.minutes == minutes } ?: NONE
        }

        fun getAllDayOptions(): List<ReminderType> {
            return entries.filter { it.isAllDayOnly || it == NONE }
        }

        fun getRegularOptions(): List<ReminderType> {
            return entries.filter { !it.isAllDayOnly }
        }
    }
}

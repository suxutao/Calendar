package com.calendar.core.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
object DateUtils {
    // 获取指定月份的所有日期（含上月尾、本月、下月初补位）
    fun getMonthDates(yearMonth: YearMonth): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        // 当月第一天
        val firstDay = yearMonth.atDay(1)
        // 当月最后一天
        val lastDay = yearMonth.atEndOfMonth()
        // 补全月初（第一行可能包含上月日期）
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 转为周一为1，周日为0
        for (i in 0 until firstDayOfWeek) {
            dates.add(firstDay.minusDays((firstDayOfWeek - i).toLong()))
        }
        // 添加当月所有日期
        for (day in 1..lastDay.dayOfMonth) {
            dates.add(yearMonth.atDay(day))
        }
        // 补全月末（最后一行可能包含下月日期）
        val lastDayOfWeek = lastDay.dayOfWeek.value % 7 // 周一为1，周日为0
        val needFill = 6 - lastDayOfWeek // 补满7列*6行=42个格子
        for (i in 1..needFill) {
            dates.add(lastDay.plusDays(i.toLong()))
        }
        return dates
    }

    // 格式化年月显示（如"2024年5月"）
    fun formatYearMonth(yearMonth: YearMonth): String {
        return yearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))
    }

    // 获取上一个月
    fun getPreviousMonth(current: YearMonth): YearMonth {
        return current.minusMonths(1)
    }

    // 获取下一个月
    fun getNextMonth(current: YearMonth): YearMonth {
        return current.plusMonths(1)
    }

    // 获取今天所在的月份
    fun getCurrentMonth(): YearMonth {
        return YearMonth.now()
    }

    // 判断日期是否为今天
    fun isToday(date: LocalDate): Boolean {
        return date.isEqual(LocalDate.now())
    }

    // 判断日期是否属于当前显示的月份
    fun isInCurrentMonth(date: LocalDate, currentMonth: YearMonth): Boolean {
        return YearMonth.from(date) == currentMonth
    }
}
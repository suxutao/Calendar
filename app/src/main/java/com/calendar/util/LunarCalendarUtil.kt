package com.calendar.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.nlf.calendar.Solar
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
object LunarCalendarUtil {

    private val monthNames = arrayOf("", "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月")
    private val dayNames = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    fun formatLunarDate(date: LocalDate): String {
        return try {
            val solar = Solar(date.year, date.monthValue, date.dayOfMonth)
            val lunar = solar.lunar

            val solarTerm = lunar.jieQi
            if (!solarTerm.isNullOrEmpty()) {
                solarTerm
            } else {
                val monthNum = lunar.month
                val dayNum = lunar.day
                val monthCn = if (monthNum in 1..12) monthNames[monthNum] else ""
                val dayCn = if (dayNum in 1..30) dayNames[dayNum - 1] else ""
                "$monthCn$dayCn"
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun formatLunarDateTiangan(date: LocalDate): String {
        return try {
            val solar = Solar(date.year, date.monthValue, date.dayOfMonth)
            val lunar = solar.lunar

            val solarTerm = lunar.jieQi
            if (!solarTerm.isNullOrEmpty()) {
                solarTerm
            } else {
                val monthNum = lunar.month
                val dayNum = lunar.day
                val monthCn = if (monthNum in 1..12) monthNames[monthNum] else ""
                val dayCn = if (dayNum in 1..30) dayNames[dayNum - 1] else ""
                "${lunar.yearInGanZhi}年$monthCn$dayCn"
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getSolarTerm(date: LocalDate): String? {
        return try {
            Solar(date.year, date.monthValue, date.dayOfMonth).lunar.jieQi
        } catch (e: Exception) {
            null
        }
    }
}

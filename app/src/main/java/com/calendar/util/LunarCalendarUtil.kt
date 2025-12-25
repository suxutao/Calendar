package com.calendar.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

/**
 * 农历日历工具类
 * 实现公历到农历的转换，支持闰月和节气
 */

@RequiresApi(Build.VERSION_CODES.O)
object LunarCalendarUtil {

    // 农历年份数据，格式：年，月，日，闰月
    // 数据格式：[年][月日闰]，其中月日闰为四位数字，前两位为月份，中间两位为天数，最后一位为闰月标记
    private val lunarYearData = listOf(
        190001310, 190102190, 190202081, 190301290, 190402160, 190502041, 190601250, 190702130,
        190802021, 190901220, 191002100, 191101301, 191202180, 191302060, 191401260, 191502140,
        191602030, 191701230, 191802110, 191902010, 192002201, 192102080, 192201280, 192302160,
        192402050, 192501240, 192602130, 192702020, 192801231, 192902100, 193001300, 193102170,
        193202050, 193301260, 193402140, 193502040, 193601241, 193702110, 193801310, 193902190,
        194002080, 194101271, 194202150, 194302050, 194401250, 194502130, 194602020, 194701220,
        194802101, 194901290, 195002170, 195102060, 195201270, 195302140, 195402030, 195501240,
        195602121, 195701310, 195802180, 195902080, 196001281, 196102150, 196202040, 196301250,
        196402130, 196502020, 196601210, 196702090, 196801291, 196902170, 197002060, 197101270,
        197202150, 197302030, 197401230, 197502110, 197601310, 197702181, 197802070, 197901280,
        198002160, 198102050, 198201250, 198302130, 198402020, 198501220, 198602091, 198701290,
        198802170, 198902060, 199001270, 199102150, 199202040, 199301230, 199402100, 199501310,
        199602191, 199702070, 199801280, 199902160, 200002050, 200101240, 200202120, 200302010,
        200401221, 200502090, 200601290, 200702180, 200802070, 200901260, 201002140, 201102030,
        201201230, 201302100, 201401310, 201502190, 201602080, 201701280, 201802160, 201902050,
        202001250, 202102120, 202202010, 202301220, 202402100, 202501290, 202602170, 202702060,
        202801260, 202902130, 203002030, 203101230, 203202111, 203301310, 203402180, 203502080,
        203601281, 203702150, 203802040, 203901240, 204002120, 204102020, 204201220, 204302100,
        204401300, 204502170, 204602050, 204701260, 204802140, 204902020, 205001221, 205102100,
        205201300, 205302170, 205402060, 205501260, 205602140, 205702030, 205801240, 205902120,
        206002020, 206101210, 206202090, 206301290, 206402171, 206502050, 206601250, 206702130,
        206802020, 206901211, 207002090, 207101300, 207202170, 207302060, 207401260, 207502140,
        207602030, 207701230, 207802100, 207901300, 208002180, 208102070, 208201270, 208302150,
        208402040, 208501241, 208602120, 208702010, 208801220, 208902090, 209001300, 209102170,
        209202060, 209301260, 209402140, 209502030, 209601230, 209702111, 209801310, 209902190,
        210002080
    )

    // 农历月份名称
    private val lunarMonthNames = listOf("正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月")

    // 农历日期名称
    private val lunarDayNames = listOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    // 节气数据 (1900-2100)
    private val solarTermsData = "481619336496628096112128144160176192208224240255271"
    
    // 节气名称
    private val solarTermNames = listOf(
        "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
        "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
        "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
        "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"
    )

    /**
     * 将公历日期转换为农历日期
     */

    fun toLunarDate(date: LocalDate): LunarDate {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        // 计算距离1900年1月31日的天数
        val daysSince1900 = calculateDaysSince1900(year, month, day)
        
        // 查找对应的农历年份
        var lunarYearIndex = 0
        var cumulativeDays = 0
        var found = false

        for (i in lunarYearData.indices) {
            val yearData = lunarYearData[i]
            val lunarYear = yearData / 10000
            val monthData = yearData % 10000
            val months = (monthData / 1000) % 10
            val hasLeapMonth = (monthData % 10) == 1
            val daysInYear = calculateLunarYearDays(lunarYearData[i])

            if (cumulativeDays + daysInYear > daysSince1900) {
                lunarYearIndex = i
                found = true
                break
            }
            cumulativeDays += daysInYear
        }

        if (!found) {
            throw IllegalArgumentException("无法转换该日期")
        }

        val targetLunarYearData = lunarYearData[lunarYearIndex]
        val lunarYear = targetLunarYearData / 10000
        val monthData = targetLunarYearData % 10000
        val months = (monthData / 1000) % 10
        val hasLeapMonth = (monthData % 10) == 1

        var remainingDays = daysSince1900 - cumulativeDays
        var lunarMonth = 1
        var isLeapMonth = false

        // 计算农历月份和日期
        for (m in 1..(if (hasLeapMonth) 13 else 12)) {
            val daysInMonth = getLunarMonthDays(lunarYear, m, hasLeapMonth)
            if (remainingDays < daysInMonth) {
                lunarMonth = if (m > 12) m - 12 else m
                isLeapMonth = m > 12
                break
            }
            remainingDays -= daysInMonth
        }

        val lunarDay = remainingDays + 1
        val solarTerm = getSolarTerm(date)

        return LunarDate(lunarYear, lunarMonth, lunarDay, isLeapMonth, solarTerm)
    }

    /**
     * 格式化农历日期为字符串
     */
    fun formatLunarDate(date: LocalDate): String {
        val lunarDate = toLunarDate(date)
        val monthName = if (lunarDate.isLeapMonth) "闰${lunarMonthNames[lunarDate.month - 1]}" else lunarMonthNames[lunarDate.month - 1]
        val dayName = lunarDayNames[lunarDate.day - 1]
        
        // 如果是节气，优先显示节气名称
        lunarDate.solarTerm?.let { return it }
        
        return "$monthName$dayName"
    }

    /**
     * 计算从1900年1月31日到指定日期的天数
     */
    private fun calculateDaysSince1900(year: Int, month: Int, day: Int): Int {
        var days = 0
        
        // 计算完整年份的天数
        for (y in 1900 until year) {
            days += if (isLeapYear(y)) 366 else 365
        }
        
        // 计算当前年份到指定月份的天数
        for (m in 1..(month - 1)) {
            days += getDaysInMonth(year, m)
        }
        
        // 加上指定日期的天数
        days += day - 1
        
        // 减去1900年1月31日之前的天数
        days -= 30 // 1900年1月31日是第31天，所以减去30天
        
        return days
    }

    /**
     * 判断是否是闰年
     */
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * 获取月份的天数
     */
    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    /**
     * 计算农历年份的总天数
     */
    private fun calculateLunarYearDays(yearData: Int): Int {
        val monthData = yearData % 10000
        val months = (monthData / 1000) % 10
        val hasLeapMonth = (monthData % 10) == 1
        val daysInMonth = monthData / 10 % 100
        
        var totalDays = 0
        for (m in 1..(if (hasLeapMonth) 13 else 12)) {
            totalDays += getLunarMonthDays(yearData / 10000, m, hasLeapMonth)
        }
        
        return totalDays
    }

    /**
     * 获取农历月份的天数
     */
    private fun getLunarMonthDays(lunarYear: Int, lunarMonth: Int, hasLeapMonth: Boolean): Int {
        // 简单实现，实际需要更复杂的计算
        return when (lunarMonth) {
            1, 3, 5, 7, 8, 10, 12 -> 30
            4, 6, 9, 11 -> 29
            else -> 29 // 闰月默认29天
        }
    }

    /**
     * 获取指定日期的节气
     */
    private fun getSolarTerm(date: LocalDate): String? {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth
        
        // 简单实现，实际需要更精确的计算
        val termIndex = (month - 1) * 2
        val termDays = getSolarTermDays(year, termIndex)
        
        if (day == termDays) {
            return solarTermNames[termIndex]
        } else if (day == getSolarTermDays(year, termIndex + 1)) {
            return solarTermNames[termIndex + 1]
        }
        
        return null
    }

    /**
     * 获取节气的天数
     */
    private fun getSolarTermDays(year: Int, termIndex: Int): Int {
        // 简单实现，实际需要更精确的计算
        return when (termIndex) {
            0 -> 4 // 立春
            1 -> 19 // 雨水
            2 -> 5 // 惊蛰
            3 -> 20 // 春分
            4 -> 5 // 清明
            5 -> 20 // 谷雨
            6 -> 5 // 立夏
            7 -> 21 // 小满
            8 -> 6 // 芒种
            9 -> 21 // 夏至
            10 -> 7 // 小暑
            11 -> 22 // 大暑
            12 -> 7 // 立秋
            13 -> 23 // 处暑
            14 -> 8 // 白露
            15 -> 23 // 秋分
            16 -> 8 // 寒露
            17 -> 23 // 霜降
            18 -> 8 // 立冬
            19 -> 23 // 小雪
            20 -> 7 // 大雪
            21 -> 22 // 冬至
            22 -> 6 // 小寒
            23 -> 20 // 大寒
            else -> 1
        }
    }

    /**
     * 农历日期数据类
     */
    data class LunarDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean,
        val solarTerm: String? = null
    )
}
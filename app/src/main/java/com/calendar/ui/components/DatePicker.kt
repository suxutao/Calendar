package com.calendar.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * 日期选择器主体（年月日滚动选择）
 * @param initialDate 初始日期
 * @param onDateSelected 日期选中回调
 * @param minYear 最小可选年份
 * @param maxYear 最大可选年份
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePicker(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    minYear: Int = 1970,
    maxYear: Int = 2100
) {
    // 选中状态管理
    var selectedYear by remember { mutableIntStateOf(initialDate.year) }
    var selectedMonth by remember { mutableIntStateOf(initialDate.monthValue) }
    var selectedDay by remember { mutableIntStateOf(initialDate.dayOfMonth) }

    // 计算当月最大天数（随年月动态变化）
    val maxDay = getMaxDayOfMonth(selectedYear, selectedMonth)

    // 滚动状态
    val yearScrollState = remember { androidx.compose.foundation.ScrollState(0) }
    val monthScrollState = remember { androidx.compose.foundation.ScrollState(0) }
    val dayScrollState = remember { androidx.compose.foundation.ScrollState(0) }

    // 像素转换（用于计算滚动位置）
    val itemHeightPx = with(LocalDensity.current) { PICKER_ITEM_HEIGHT.toPx() }

    // 初始滚动到默认日期位置
    LaunchedEffect(Unit) {
        yearScrollState.scrollTo((selectedYear - minYear) * itemHeightPx.roundToInt())
        monthScrollState.scrollTo((selectedMonth - 1) * itemHeightPx.roundToInt())
        dayScrollState.scrollTo((selectedDay - 1) * itemHeightPx.roundToInt())
    }

    // 监听年份滚动更新选中值
    LaunchedEffect(yearScrollState.value) {
        // 去掉+0.5f，直接取整后修正（解决向上取整偏差）
        val centerIndex = (yearScrollState.value / itemHeightPx).toInt()
        // 修正：当滚动超过item的1/4高度时，视为下一个索引
        val adjustedIndex = if (yearScrollState.value % itemHeightPx > itemHeightPx * 0.25f) {
            centerIndex + 1
        } else {
            centerIndex
        }
        val newYear = minYear + adjustedIndex
        if (newYear in minYear..maxYear) {
            selectedYear = newYear
        }
    }

    // 监听月份滚动更新选中值
    LaunchedEffect(monthScrollState.value) {
        val centerIndex = (monthScrollState.value / itemHeightPx).toInt()
        val adjustedIndex = if (monthScrollState.value % itemHeightPx > itemHeightPx * 0.25f) {
            centerIndex + 1
        } else {
            centerIndex
        }
        val newMonth = adjustedIndex + 1 // 月份1-12，索引+1
        if (newMonth in 1..12) {
            selectedMonth = newMonth
        }
    }

    // 监听日期滚动更新选中值（核心修复）
    LaunchedEffect(dayScrollState.value, selectedYear, selectedMonth) {
        val currentMaxDay = getMaxDayOfMonth(selectedYear, selectedMonth)
        val centerIndex = (dayScrollState.value / itemHeightPx).toInt()
        // 关键：当滚动超过item的1/4高度时，才视为下一个日期
        val adjustedIndex = if (dayScrollState.value % itemHeightPx > itemHeightPx * 0.25f) {
            centerIndex + 1
        } else {
            centerIndex
        }
        selectedDay = (adjustedIndex + 1).coerceIn(1, currentMaxDay) // 日期1-31，索引+1
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 单位标签（年/月/日）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                "年",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "月",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "日",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // 滚动选择区域（限制高度，显示中间选中项）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PICKER_ITEM_HEIGHT * VISIBLE_ITEMS_COUNT)
        ) {
            // 选中项高亮背景（固定在中间）
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(PICKER_ITEM_HEIGHT)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    )
            )

            // 年/月/日三列滚动选择器
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 年份选择
                ScrollPicker(
                    items = (minYear..maxYear).map { it.toString() },
                    scrollState = yearScrollState,
                    modifier = Modifier.width(60.dp)
                )

                // 月份选择
                ScrollPicker(
                    items = (1..12).map { it.toString() },
                    scrollState = monthScrollState,
                    modifier = Modifier.width(60.dp)
                )

                // 日期选择（动态生成当月天数）
                ScrollPicker(
                    items = (1..maxDay).map { it.toString() },
                    scrollState = dayScrollState,
                    modifier = Modifier.width(60.dp)
                )
            }
        }

        // 确认按钮（触发回调）
        LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
            onDateSelected(LocalDate.of(selectedYear, selectedMonth, selectedDay))
        }
    }
}

// 工具函数：计算当月最大天数
private fun getMaxDayOfMonth(year: Int, month: Int): Int {
    return when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

// 工具函数：判断闰年
private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
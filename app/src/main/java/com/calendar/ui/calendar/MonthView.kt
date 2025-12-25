package com.calendar.ui.calendar

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.model.Schedule
import com.calendar.ui.components.CalendarDay
import com.calendar.ui.components.DateCell
import com.calendar.ui.components.WeekdaysHeader
import com.calendar.viewmodel.ScheduleViewModel
import com.calendar.util.LunarCalendarUtil
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    initialSelectedDate: LocalDate? = null,
    onAddScheduleClick: (LocalDate) -> Unit = {}
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var currentDate by remember { mutableStateOf(initialSelectedDate ?: selectedDate) }
    var currentMonth by remember { mutableStateOf(initialSelectedDate ?: selectedDate) }
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val allSchedules by scheduleViewModel.allSchedules.collectAsState(initial = emptyList())

    // 监听 initialSelectedDate 变化，强制更新选中日期
    // 使用initialSelectedDate或selectedDate作为目标日期
    val targetDate = initialSelectedDate ?: selectedDate
    
    // 当targetDate变化时，更新currentMonth和currentDate
    LaunchedEffect(targetDate) {
        currentMonth = targetDate
        currentDate = targetDate
    }

    // 过滤出当月的日程并按日期分组
    val monthSchedules = allSchedules.filter { schedule ->
        val scheduleDate = java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        scheduleDate.month == currentMonth.month && scheduleDate.year == currentMonth.year
    }.groupBy { schedule ->
        java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 月份标题栏
            MonthHeader(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )
            
            // 星期标题
            WeekdaysHeader()
            
            // 日历网格
            MonthGrid(
                currentMonth = currentMonth,
                currentDate = currentDate,
                today = today,
                monthSchedules = monthSchedules,
                onDateClick = { date ->
                    currentDate = date
                    onDateSelected(date)
                }
            )
        }

        // 今天按钮
        FloatingActionButton(
            onClick = {
                currentDate = today
                currentMonth = today
                onDateSelected(today)
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "切换到今天"
            )
        }

        // 添加日程按钮
        FloatingActionButton(
            onClick = { onAddScheduleClick(currentDate) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加日程"
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthHeader(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一月"
            )
        }
        
        Text(
            text = currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月")),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一月"
            )
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthGrid(
    currentMonth: LocalDate,
    currentDate: LocalDate,
    today: LocalDate,
    monthSchedules: Map<LocalDate, List<Schedule>>,
    onDateClick: (LocalDate) -> Unit
) {
    // 获取月份的第一天
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    // 获取月份的第一天是星期几
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0-6, 0是周日
    // 获取月份的天数
    val daysInMonth = currentMonth.lengthOfMonth()
    // 获取上个月的天数
    val daysInPreviousMonth = currentMonth.minusMonths(1).lengthOfMonth()
    
    // 创建日历网格数据
    val calendarDays = mutableListOf<CalendarDay>()
    
    // 添加上个月的日期
    for (i in firstDayOfWeek downTo 1) {
        val day = currentMonth.minusMonths(1).withDayOfMonth(daysInPreviousMonth - i + 1)
        calendarDays.add(CalendarDay(day, isCurrentMonth = false, isToday = day == today))
    }
    
    // 添加当月的日期
    for (i in 1..daysInMonth) {
        val day = currentMonth.withDayOfMonth(i)
        calendarDays.add(CalendarDay(day, isCurrentMonth = true, isToday = day == today))
    }
    
    // 添加下个月的日期，填充完整的6行
    val remainingDays = 42 - calendarDays.size // 6行 x 7列 = 42个格子
    for (i in 1..remainingDays) {
        val day = currentMonth.plusMonths(1).withDayOfMonth(i)
        calendarDays.add(CalendarDay(day, isCurrentMonth = false, isToday = day == today))
    }
    
    Column(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        for (row in 0..5) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    val calendarDay = calendarDays[index]
                    val scheduleCount = monthSchedules[calendarDay.date]?.size ?: 0
                    
                    DateCell(
                        calendarDay = calendarDay,
                        isSelected = calendarDay.date == currentDate,
                        scheduleCount = scheduleCount,
                        onDateClick = onDateClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


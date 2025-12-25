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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onAddScheduleClick: (LocalDate) -> Unit = {}
) {
    val today = LocalDate.now()
    var currentDate by remember { mutableStateOf(selectedDate) }
    var currentWeekStart by remember { mutableStateOf(getWeekStart(selectedDate)) }
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val allSchedules by scheduleViewModel.allSchedules.collectAsState(initial = emptyList())

    // 过滤出当周的日程并按日期分组
    val weekSchedules = allSchedules.filter { schedule ->
        val scheduleDate = Instant.ofEpochMilli(schedule.startTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val weekEnd = currentWeekStart.plusDays(6)
        scheduleDate in currentWeekStart..weekEnd
    }.groupBy { schedule ->
        Instant.ofEpochMilli(schedule.startTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 周标题栏
            WeekHeader(
                currentWeekStart = currentWeekStart,
                onPreviousWeek = { currentWeekStart = currentWeekStart.minusDays(7) },
                onNextWeek = { currentWeekStart = currentWeekStart.plusDays(7) }
            )
            
            // 星期标题
            WeekdaysHeader()
            
            // 日历网格
            WeekGrid(
                currentWeekStart = currentWeekStart,
                currentDate = currentDate,
                today = today,
                weekSchedules = weekSchedules,
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
                currentWeekStart = getWeekStart(today)
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
fun WeekHeader(
    currentWeekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val weekEnd = currentWeekStart.plusDays(6)
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MM月dd日")
    val weekTitle = "${currentWeekStart.format(formatter)} - ${weekEnd.format(formatter)}"

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一周"
            )
        }
        
        Text(
            text = weekTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(onClick = onNextWeek) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一周"
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekGrid(
    currentWeekStart: LocalDate,
    currentDate: LocalDate,
    today: LocalDate,
    weekSchedules: Map<LocalDate, List<Schedule>>,
    onDateClick: (LocalDate) -> Unit
) {
    // 创建一周的日期列表
    val weekDays = mutableListOf<CalendarDay>()
    for (i in 0..6) {
        val date = currentWeekStart.plusDays(i.toLong())
        weekDays.add(CalendarDay(date, isCurrentMonth = true, isToday = date == today))
    }
    
    Column(modifier = Modifier.fillMaxWidth().height(80.dp)) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            weekDays.forEach { calendarDay ->
                val scheduleCount = weekSchedules[calendarDay.date]?.size ?: 0
                
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

// 获取一周的开始日期（周日）
@RequiresApi(Build.VERSION_CODES.O)
private fun getWeekStart(date: LocalDate): LocalDate {
    return when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> date
        else -> date.minusDays(date.dayOfWeek.value.toLong())
    }
}


package com.calendar.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.model.Schedule
import com.calendar.ui.components.CalendarDay
import com.calendar.ui.components.DateCell
import com.calendar.ui.components.WeekdaysHeader
import com.calendar.viewmodel.ScheduleViewModel
import com.calendar.util.LunarCalendarUtil
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

    val targetDate = initialSelectedDate ?: selectedDate
    
    LaunchedEffect(targetDate) {
        currentMonth = targetDate
        currentDate = targetDate
    }

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                MonthHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )

                WeekdaysHeader()

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

                Spacer(modifier = Modifier.height(80.dp))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    onClick = {
                        currentDate = today
                        currentMonth = today
                        onDateSelected(today)
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "切换到今天"
                    )
                }

                FloatingActionButton(
                    onClick = { onAddScheduleClick(currentDate) },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加日程"
                    )
                }
            }
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "上一月"
                )
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年 M月")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            FilledTonalIconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "下一月"
                )
            }
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
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val daysInPreviousMonth = currentMonth.minusMonths(1).lengthOfMonth()
    
    val calendarDays = mutableListOf<CalendarDay>()
    
    for (i in firstDayOfWeek downTo 1) {
        val day = currentMonth.minusMonths(1).withDayOfMonth(daysInPreviousMonth - i + 1)
        calendarDays.add(CalendarDay(day, isCurrentMonth = false, isToday = day == today))
    }
    
    for (i in 1..daysInMonth) {
        val day = currentMonth.withDayOfMonth(i)
        calendarDays.add(CalendarDay(day, isCurrentMonth = true, isToday = day == today))
    }
    
    val remainingDays = 42 - calendarDays.size
    for (i in 1..remainingDays) {
        val day = currentMonth.plusMonths(1).withDayOfMonth(i)
        calendarDays.add(CalendarDay(day, isCurrentMonth = false, isToday = day == today))
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            for (row in 0..5) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (col in 0..6) {
                        val index = row * 7 + col
                        val calendarDay = calendarDays[index]
                        val scheduleCount = monthSchedules[calendarDay.date]?.size ?: 0
                        
                        DateCellImproved(
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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateCellImproved(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    scheduleCount: Int,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val date = calendarDay.date
    val lunarDate = LunarCalendarUtil.formatLunarDate(date)
    
    val textColor = when {
        !calendarDay.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        calendarDay.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        calendarDay.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val selectedTextColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        textColor
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onDateClick(date) }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (calendarDay.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = selectedTextColor
            )
            
            if (lunarDate.isNotEmpty()) {
                Text(
                    text = lunarDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) selectedTextColor.copy(alpha = 0.8f) else textColor.copy(alpha = 0.7f)
                )
            }
            
            if (scheduleCount > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(minOf(scheduleCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                        if (it < minOf(scheduleCount, 3) - 1) {
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                    if (scheduleCount > 3) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) selectedTextColor.copy(alpha = 0.8f) else textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}


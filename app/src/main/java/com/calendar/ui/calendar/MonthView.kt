package com.calendar.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.model.Schedule
import com.calendar.ui.components.CalendarDay
import com.calendar.ui.components.WeekdaysHeader
import com.calendar.ui.settings.PreferencesManager
import com.calendar.util.LunarCalendarUtil
import com.calendar.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    var currentMonth by remember { mutableStateOf(initialSelectedDate ?: selectedDate) }
    var previousMonth by remember { mutableStateOf(currentMonth.minusMonths(1)) }
    var nextMonth by remember { mutableStateOf(currentMonth.plusMonths(1)) }
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val allSchedules by scheduleViewModel.allSchedules.collectAsState(initial = emptyList())

    val prefs = remember { PreferencesManager(context) }
    val showLunarCalendar by remember { mutableStateOf(prefs.showLunarCalendar) }

    val targetDate = initialSelectedDate ?: selectedDate

    LaunchedEffect(targetDate) {
        currentMonth = targetDate
        previousMonth = targetDate.minusMonths(1)
        nextMonth = targetDate.plusMonths(1)
    }

    val currentSchedules = allSchedules.filter { schedule ->
        val scheduleDate = java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        scheduleDate.month == currentMonth.month && scheduleDate.year == currentMonth.year
    }.groupBy { schedule ->
        java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }

    val previousSchedules = allSchedules.filter { schedule ->
        val scheduleDate = java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        scheduleDate.month == previousMonth.month && scheduleDate.year == previousMonth.year
    }.groupBy { schedule ->
        java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }

    val nextSchedules = allSchedules.filter { schedule ->
        val scheduleDate = java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        scheduleDate.month == nextMonth.month && scheduleDate.year == nextMonth.year
    }.groupBy { schedule ->
        java.time.Instant.ofEpochMilli(schedule.startTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }

    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val currentAlpha = remember { Animatable(1f) }
    var isAnimating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (!isAnimating) {
                            isAnimating = true
                            if (offsetY > swipeThreshold) {
                                scope.launch {
                                    currentAlpha.animateTo(0f, tween(150))
                                    nextMonth = currentMonth
                                    currentMonth = previousMonth
                                    previousMonth = currentMonth.minusMonths(1)
                                    currentAlpha.snapTo(0f)
                                    currentAlpha.animateTo(1f, tween(150))
                                    onDateSelected(currentMonth)
                                    isAnimating = false
                                }
                            } else if (offsetY < -swipeThreshold) {
                                scope.launch {
                                    currentAlpha.animateTo(0f, tween(150))
                                    previousMonth = currentMonth
                                    currentMonth = nextMonth
                                    nextMonth = currentMonth.plusMonths(1)
                                    currentAlpha.snapTo(0f)
                                    currentAlpha.animateTo(1f, tween(150))
                                    onDateSelected(currentMonth)
                                    isAnimating = false
                                }
                            } else {
                                isAnimating = false
                            }
                        }
                        offsetY = 0f
                    },
                    onDragCancel = {
                        offsetY = 0f
                        isAnimating = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        offsetY = offsetY + dragAmount
                    }
                )
            }
    ) {
        MonthHeader(
            currentMonth = currentMonth,
            onPreviousMonth = {
                currentMonth = currentMonth.minusMonths(1)
                onDateSelected(currentMonth)
            },
            onNextMonth = {
                currentMonth = currentMonth.plusMonths(1)
                onDateSelected(currentMonth)
            }
        )

        Spacer(modifier = Modifier.height(50.dp))

        WeekdaysHeader()

        MonthGrid(
            currentMonth = currentMonth,
            currentDate = currentMonth,
            today = today,
            monthSchedules = currentSchedules,
            showLunarCalendar = showLunarCalendar,
            onDateClick = { date ->
                onDateSelected(date)
            },
            modifier = Modifier.alpha(currentAlpha.value)
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FloatingActionButton(
                onClick = {
                    currentMonth = today
                    onDateSelected(today)
                },
                modifier = Modifier.size(60.dp),
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
                onClick = { onAddScheduleClick(currentMonth) },
                modifier = Modifier.size(60.dp),
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthContent(
    currentMonth: LocalDate,
    currentDate: LocalDate,
    today: LocalDate,
    monthSchedules: Map<LocalDate, List<Schedule>>,
    showLunarCalendar: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            WeekdaysHeader()

            MonthGrid(
                currentMonth = currentMonth,
                currentDate = currentDate,
                today = today,
                monthSchedules = monthSchedules,
                showLunarCalendar = showLunarCalendar,
                onDateClick = onDateClick
            )

            Spacer(modifier = Modifier.height(80.dp))
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
    showLunarCalendar: Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
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
                            showLunarCalendar = showLunarCalendar,
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
    showLunarCalendar: Boolean,
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

            if (showLunarCalendar && lunarDate.isNotEmpty()) {
                Text(
                    text = lunarDate.takeLast(2),
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


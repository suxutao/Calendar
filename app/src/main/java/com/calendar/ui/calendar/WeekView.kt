package com.calendar.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.model.Schedule
import com.calendar.ui.components.CalendarDay
import com.calendar.viewmodel.ScheduleViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onAddScheduleClick: (LocalDate) -> Unit = {},
    onScheduleClick: (Schedule) -> Unit = {}
) {
    val today = LocalDate.now()
    var currentDate by remember { mutableStateOf(selectedDate) }
    var currentWeekStart by remember { mutableStateOf(getWeekStart(selectedDate)) }
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val allSchedules by scheduleViewModel.allSchedules.collectAsState(initial = emptyList())

    val weekDays = remember(currentWeekStart) {
        (0..6).map { offset ->
            val date = currentWeekStart.plusDays(offset.toLong())
            CalendarDay(date, isCurrentMonth = true, isToday = date == today)
        }
    }

    val weekSchedules = remember(allSchedules, currentWeekStart) {
        allSchedules.filter { schedule ->
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
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeekHeader(
            currentWeekStart = currentWeekStart,
            onPreviousWeek = {
                currentWeekStart = currentWeekStart.minusDays(7)
                currentDate = currentDate.minusDays(7)
            },
            onNextWeek = {
                currentWeekStart = currentWeekStart.plusDays(7)
                currentDate = currentDate.plusDays(7)
            },
            onTodayClick = {
                currentWeekStart = getWeekStart(today)
                currentDate = today
                onDateSelected(today)
            }
        )

        WeekDayHeader()

        Box(modifier = Modifier.fillMaxSize()) {
            WeekScheduleGrid(
                weekDays = weekDays,
                currentDate = currentDate,
                weekSchedules = weekSchedules,
                onDateClick = { date ->
                    currentDate = date
                    onDateSelected(date)
                },
                onScheduleClick = onScheduleClick
            )

            FloatingActionButton(
                onClick = { onAddScheduleClick(currentDate) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp),
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
fun WeekHeader(
    currentWeekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit
) {
    val weekEnd = currentWeekStart.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("M月d日")
    val weekTitle = "${currentWeekStart.format(formatter)} - ${weekEnd.format(formatter)}"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "上一周",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = weekTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row {
                IconButton(onClick = onTodayClick) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "今天",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNextWeek) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "下一周",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekDayHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")
            daysOfWeek.forEachIndexed { index, day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (index == 0 || index == 6) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekScheduleGrid(
    weekDays: List<CalendarDay>,
    currentDate: LocalDate,
    weekSchedules: Map<LocalDate, List<Schedule>>,
    onDateClick: (LocalDate) -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
    ) {
        weekDays.forEach { calendarDay ->
            val schedules = weekSchedules[calendarDay.date]?.sortedBy { it.startTime } ?: emptyList()

            DayScheduleRow(
                calendarDay = calendarDay,
                isSelected = calendarDay.date == currentDate,
                schedules = schedules,
                onDateClick = onDateClick,
                onScheduleClick = onScheduleClick
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayScheduleRow(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    schedules: List<Schedule>,
    onDateClick: (LocalDate) -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onDateClick(calendarDay.date) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(52.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = calendarDay.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (calendarDay.isToday) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = if (calendarDay.isToday) {
                        MaterialTheme.colorScheme.primary
                    } else if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = calendarDay.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                calendarDay.isToday -> MaterialTheme.colorScheme.onPrimary
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (schedules.isEmpty()) {
                    Text(
                        text = "无日程",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    schedules.take(3).forEach { schedule ->
                        ScheduleBriefCard(
                            schedule = schedule,
                            timeFormatter = timeFormatter,
                            onClick = { onScheduleClick(schedule) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (schedules.size > 3) {
                        Text(
                            text = "还有${schedules.size - 3}个日程",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleBriefCard(
    schedule: Schedule,
    timeFormatter: DateTimeFormatter,
    onClick: () -> Unit = {}
) {
    val startDateTime = Instant.ofEpochMilli(schedule.startTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    Instant.ofEpochMilli(schedule.endTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val containerColor = if (schedule.isAllDay) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (schedule.isAllDay) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!schedule.isAllDay) {
                Text(
                    text = startDateTime.format(timeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    modifier = Modifier.width(48.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = schedule.title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getWeekStart(date: LocalDate): LocalDate {
    return when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> date
        else -> date.minusDays(date.dayOfWeek.value.toLong())
    }
}

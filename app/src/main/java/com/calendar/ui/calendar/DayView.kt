package com.calendar.ui.calendar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.model.Schedule
import com.calendar.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayView(
    selectedDate: LocalDate = LocalDate.now(),
    onAddScheduleClick: (LocalDate) -> Unit = {},
    onScheduleClick: (Schedule) -> Unit = {}
) {
    val viewModel: ScheduleViewModel = viewModel()
    val allSchedules by viewModel.allSchedules.collectAsState(initial = emptyList())

    var currentDate by remember { mutableStateOf(selectedDate) }
    val todaySchedules = allSchedules.filter { schedule ->
        val scheduleStart = Instant.ofEpochMilli(schedule.startTime).atZone(ZoneId.systemDefault())
        val scheduleEnd = Instant.ofEpochMilli(schedule.endTime).atZone(ZoneId.systemDefault())
        val currentStart = currentDate.atStartOfDay(ZoneId.systemDefault())
        val currentEnd = currentDate.plusDays(1).atStartOfDay(ZoneId.systemDefault())

        if (schedule.isAllDay) {
            val scheduleDate = scheduleStart.toLocalDate()
            scheduleDate == currentDate
        } else {
            scheduleStart.isBefore(currentEnd) && scheduleEnd.isAfter(currentStart)
        }
    }.sortedBy { schedule -> schedule.startTime }

    LaunchedEffect(selectedDate) {
        currentDate = selectedDate
        viewModel.setSelectedDate(currentDate)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayHeader(
                date = currentDate,
                onPreviousDay = { currentDate = currentDate.minusDays(1) },
                onNextDay = { currentDate = currentDate.plusDays(1) }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    TimeAxis()

                    ScheduleArea(
                        schedules = todaySchedules,
                        onScheduleClick = onScheduleClick
                    )
                }

                FloatingActionButton(
                    onClick = { onAddScheduleClick(currentDate) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
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
fun DayHeader(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "前一天",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("yyyy年 M月d日")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            IconButton(onClick = onNextDay) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "后一天",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TimeAxis() {
    Surface(
        modifier = Modifier.width(56.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            (0..23).forEach { hour ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = String.format("%02d:00", hour),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleArea(
    schedules: List<Schedule>,
    onScheduleClick: (Schedule) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                (0..23).forEach { _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        schedules.forEachIndexed { index, schedule ->
            ScheduleCard(
                schedule = schedule,
                schedules = schedules,
                index = index,
                totalCount = schedules.size,
                onScheduleClick = { onScheduleClick(schedule) }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleCard(
    schedule: Schedule,
    schedules: List<Schedule>,
    index: Int,
    totalCount: Int,
    onScheduleClick: () -> Unit
) {
    val startTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(schedule.startTime),
        ZoneId.systemDefault()
    ).toLocalTime()
    val endTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(schedule.endTime),
        ZoneId.systemDefault()
    ).toLocalTime()

    val startMinutes = startTime.hour * 60 + startTime.minute
    val endMinutes = endTime.hour * 60 + endTime.minute
    val itemHeight = maxOf((endMinutes - startMinutes).dp, 50.dp)
    val topOffset = startMinutes.dp

    val columnCount = calculateColumnCount(schedules = schedules, currentIndex = index)
    val columnWidth = 1f / columnCount
    val startColumn = calculateStartColumn(schedules = schedules, currentIndex = index)
    val cardOffset = (startColumn * columnWidth * 100).toInt() / 100f + 0.02f
    val cardWidth = columnWidth - 0.04f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1440.dp)
            .padding(start = 56.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = topOffset)
                .fillMaxWidth(cardWidth)
                .height(itemHeight)
                .padding(start = (cardOffset * 100).dp.coerceAtMost(16.dp))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onScheduleClick() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(4.dp)
                                .height(20.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {}

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = schedule.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (schedule.description != null && schedule.description.isNotEmpty()) {
                                Text(
                                    text = schedule.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Text(
                                text = String.format("%02d:%02d - %02d:%02d",
                                    startTime.hour, startTime.minute,
                                    endTime.hour, endTime.minute),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateColumnCount(schedules: List<Schedule>, currentIndex: Int): Int {
    val currentSchedule = schedules[currentIndex]
    val currentStart = Instant.ofEpochMilli(currentSchedule.startTime).atZone(ZoneId.systemDefault())
    val currentEnd = Instant.ofEpochMilli(currentSchedule.endTime).atZone(ZoneId.systemDefault())

    var maxOverlaps = 1
    var overlapCount = 1

    for (i in 0 until currentIndex) {
        val otherSchedule = schedules[i]
        val otherStart = Instant.ofEpochMilli(otherSchedule.startTime).atZone(ZoneId.systemDefault())
        val otherEnd = Instant.ofEpochMilli(otherSchedule.endTime).atZone(ZoneId.systemDefault())

        if (currentStart.isBefore(otherEnd) && currentEnd.isAfter(otherStart)) {
            overlapCount++
            maxOverlaps = maxOf(maxOf(overlapCount, maxOverlaps), 1)
        } else {
            overlapCount = 1
        }
    }

    return maxOverlaps.coerceAtMost(3)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateStartColumn(schedules: List<Schedule>, currentIndex: Int): Int {
    val currentSchedule = schedules[currentIndex]
    val currentStart = Instant.ofEpochMilli(currentSchedule.startTime).atZone(ZoneId.systemDefault())
    val currentEnd = Instant.ofEpochMilli(currentSchedule.endTime).atZone(ZoneId.systemDefault())

    var column = 0
    val usedColumns = mutableSetOf<Int>()

    for (i in 0 until currentIndex) {
        val otherSchedule = schedules[i]
        val otherStart = Instant.ofEpochMilli(otherSchedule.startTime).atZone(ZoneId.systemDefault())
        val otherEnd = Instant.ofEpochMilli(otherSchedule.endTime).atZone(ZoneId.systemDefault())

        if (currentStart.isBefore(otherEnd) && currentEnd.isAfter(otherStart)) {
            val endMinutes = otherEnd.toLocalTime().hour * 60 + otherEnd.toLocalTime().minute
            val startMinutes = currentStart.toLocalTime().hour * 60 + currentStart.toLocalTime().minute

            if (endMinutes <= startMinutes) {
                usedColumns.remove(0)
            } else {
                val overlapColumn = (i % 3)
                usedColumns.add(overlapColumn)
            }
        }
    }

    while (usedColumns.contains(column)) {
        column++
    }

    return column % 3
}

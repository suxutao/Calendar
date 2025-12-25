package com.calendar.ui.calendar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.model.Schedule
import com.calendar.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayView(
    selectedDate: LocalDate = LocalDate.now(),
    onAddScheduleClick: (LocalDate) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ScheduleViewModel = viewModel()
    viewModel.setSelectedDate(selectedDate)
    val date by viewModel.selectedDate.observeAsState(initial = selectedDate)
    val allSchedules by viewModel.allSchedules.collectAsState(initial = emptyList())
    
    val todaySchedules = allSchedules.filter { schedule ->
        val scheduleDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(schedule.startTime),
            ZoneId.systemDefault()
        ).toLocalDate()
        scheduleDate == date
    }.sortedBy { schedule -> schedule.startTime }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayHeader(date = date)
            
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TimeAxis()
                    
                    ScheduleArea(
                        schedules = todaySchedules,
                        onScheduleClick = { },
                        onScheduleDelete = { }
                    )
                }
                
                FloatingActionButton(
                    onClick = { onAddScheduleClick(date) },
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
fun DayHeader(date: LocalDate) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
    onScheduleClick: (Schedule) -> Unit,
    onScheduleDelete: (Schedule) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                (0..23).forEach { hour ->
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
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp)
        ) {
            items(schedules, key = { it.id }) { schedule ->
                ScheduleItemImproved(
                    schedule = schedule,
                    onClick = { onScheduleClick(schedule) },
                    onDelete = { onScheduleDelete(schedule) }
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleItemImproved(
    schedule: Schedule,
    onClick: () -> Unit,
    onDelete: () -> Unit
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
    val height = maxOf((endMinutes - startMinutes).dp, 50.dp)
    
    Card(
        modifier = Modifier
            .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .offset(y = startMinutes.dp)
            .fillMaxWidth()
            .height(height)
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onDelete() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
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
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "详情",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
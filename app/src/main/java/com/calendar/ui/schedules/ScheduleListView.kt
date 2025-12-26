package com.calendar.ui.schedules

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calendar.model.Schedule
import com.calendar.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleListView(
    selectedDate: LocalDate,
    viewModel: ScheduleViewModel,
    onScheduleClick: (Schedule) -> Unit = {}
) {
    LocalContext.current
    val allSchedules by viewModel.allSchedules.collectAsState(initial = emptyList())

    // 过滤出选中日期的日程
    val filteredSchedules = allSchedules.filter {
        val scheduleDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(it.startTime),
            ZoneId.systemDefault()
        ).toLocalDate()
        scheduleDate == selectedDate
    }.sortedBy { it.startTime }

    Column(modifier = Modifier.fillMaxSize()) {
        // 日程列表标题
        Text(
            text = "日程列表",
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )

        // 日程列表
        if (filteredSchedules.isEmpty()) {
            Text(
                text = "当天没有日程",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Gray
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredSchedules) { schedule ->
                    ScheduleListItem(
                        schedule = schedule,
                        onClick = { onScheduleClick(schedule) }
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleListItem(
    schedule: Schedule,
    onClick: () -> Unit
) {
    val startTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(schedule.startTime),
        ZoneId.systemDefault()
    )
    val endTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(schedule.endTime),
        ZoneId.systemDefault()
    )

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = schedule.title,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (schedule.description != null) {
                Text(
                    text = schedule.description,
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = "${timeFormatter.format(startTime)} - ${timeFormatter.format(endTime)}",
                fontSize = 14.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

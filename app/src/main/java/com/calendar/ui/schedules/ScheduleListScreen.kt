package com.calendar.ui.schedules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calendar.model.Schedule
import com.calendar.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    viewModel: ScheduleViewModel,
    onNavigateBack: () -> Unit,
    onEditSchedule: (Schedule) -> Unit
) {
    val allSchedules by viewModel.allSchedules.collectAsState(initial = emptyList())
    var showEditScreen by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<Schedule?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val groupedSchedules = remember(allSchedules) {
        allSchedules
            .groupBy { schedule ->
                Instant.ofEpochMilli(schedule.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .toSortedMap(compareByDescending { it })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "日程列表",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        if (allSchedules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(100.dp)
                        .width(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    tonalElevation = 1.dp
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无日程",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击下方添加按钮创建日程",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                groupedSchedules.forEach { (date, schedules) ->
                    val sortedSchedules = schedules.sortedBy { it.startTime }
                    val displayDate = if (date == LocalDate.now()) "今天" else date.format(dateFormatter)

                    item(key = "header_$date") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = displayDate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${schedules.size}个日程",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    items(sortedSchedules, key = { it.id }) { schedule ->
                        ScheduleListItem(
                            schedule = schedule,
                            onClick = {
                                editingSchedule = schedule
                                showEditScreen = true
                            },
                            timeFormatter = timeFormatter
                        )
                    }

                    item(key = "spacer_$date") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showEditScreen && editingSchedule != null) {
        EditScheduleScreen(
            schedule = editingSchedule!!,
            onNavigateBack = {
                showEditScreen = false
                editingSchedule = null
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun ScheduleListItem(
    schedule: Schedule,
    onClick: () -> Unit,
    timeFormatter: DateTimeFormatter
) {
    val startDateTime = Instant.ofEpochMilli(schedule.startTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val endDateTime = Instant.ofEpochMilli(schedule.endTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(2.dp),
                color = if (schedule.isAllDay) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ) {}

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!schedule.description.isNullOrBlank()) {
                    Text(
                        text = schedule.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .height(14.dp)
                            .width(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (schedule.isAllDay) {
                            "全天"
                        } else {
                            "${startDateTime.format(timeFormatter)} - ${endDateTime.format(timeFormatter)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "编辑",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

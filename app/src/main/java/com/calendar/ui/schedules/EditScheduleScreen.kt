package com.calendar.ui.schedules

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.calendar.model.ReminderType
import com.calendar.model.Schedule
import com.calendar.ui.components.showToast
import com.calendar.viewmodel.ScheduleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    schedule: Schedule,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleViewModel
) {
    val context = LocalContext.current
    val startDateTime = Instant.ofEpochMilli(schedule.startTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val endDateTime = Instant.ofEpochMilli(schedule.endTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    var title by remember { mutableStateOf(TextFieldValue(schedule.title)) }
    var description by remember { mutableStateOf(TextFieldValue(schedule.description ?: "")) }
    var startHour by remember { mutableStateOf(startDateTime.hour) }
    var startMinute by remember { mutableStateOf(startDateTime.minute) }
    var endHour by remember { mutableStateOf(endDateTime.hour) }
    var endMinute by remember { mutableStateOf(endDateTime.minute) }
    var isAllDay by remember { mutableStateOf(schedule.isAllDay) }
    var reminderType by remember { mutableStateOf(schedule.reminderType) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑日程") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteSchedule(schedule)
                            showToast(context, "日程已删除")
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                placeholder = { Text("请输入日程标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述") },
                placeholder = { Text("请输入日程描述（可选）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "时间设置",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("全天事件")
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it }
                        )
                    }

                    if (!isAllDay) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始时间")
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = String.format("%02d:%02d", startHour, startMinute),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("结束时间")
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = String.format("%02d:%02d", endHour, endMinute),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.text.isBlank()) {
                        showToast(context, "请输入日程标题")
                        return@Button
                    }

                    try {
                        val selectedDate = startDateTime.toLocalDate()
                        val startDT = if (isAllDay) {
                            selectedDate.atStartOfDay()
                        } else {
                            LocalDateTime.of(selectedDate, LocalTime.of(startHour, startMinute))
                        }
                        val endDT = if (isAllDay) {
                            selectedDate.plusDays(1).atStartOfDay()
                        } else {
                            LocalDateTime.of(selectedDate, LocalTime.of(endHour, endMinute))
                        }

                        if (endDT.isBefore(startDT)) {
                            showToast(context, "结束时间不能早于开始时间")
                            return@Button
                        }

                        val updatedSchedule = schedule.copy(
                            title = title.text.trim(),
                            description = description.text.takeIf { it.isNotBlank() }?.trim(),
                            startTime = startDT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            endTime = endDT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            reminderType = reminderType,
                            isAllDay = isAllDay
                        )

                        viewModel.updateSchedule(updatedSchedule)
                        showToast(context, "日程已更新")
                        onNavigateBack()
                    } catch (e: Exception) {
                        showToast(context, "时间设置错误，请检查")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存更改")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("取消")
            }
        }
    }
}

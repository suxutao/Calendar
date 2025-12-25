package com.calendar.ui.calendar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.calendar.model.Schedule
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    
    // 过滤出当天的日程
    val todaySchedules = allSchedules.filter { schedule ->
        val scheduleDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(schedule.startTime),
            ZoneId.systemDefault()
        ).toLocalDate()
        scheduleDate == date
    }.sortedBy { schedule -> schedule.startTime }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 日期标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE")),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 日视图内容
        Row(modifier = Modifier.fillMaxSize()) {
            // 时间轴
            TimeAxis()
            
            // 日程区域
            ScheduleArea(
                schedules = todaySchedules,
                onScheduleLongClick = { /* 编辑日程逻辑 */ },
                onScheduleDelete = { /* 删除日程逻辑 */ }
            )
        }
        
        // 添加日程按钮
        FloatingActionButton(
            onClick = { onAddScheduleClick(date) },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
        ) {
            Text(text = "+", fontSize = 24.sp)
        }
    }
}

/**
 * 时间轴组件
 */
@Composable
fun TimeAxis() {
    Column(modifier = Modifier.width(60.dp)) {
        (0..23).forEach { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = String.format("%02d:00", hour),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

/**
 * 日程区域组件
 */
@Composable
fun ScheduleArea(
    schedules: List<Schedule>,
    onScheduleLongClick: (Schedule) -> Unit,
    onScheduleDelete: (Schedule) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.1f))) {
        // 小时分隔线
        (0..23).forEach { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .offset(y = (hour * 60).dp)
                    .border(width = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            )
        }
        
        // 显示日程
        Column(modifier = Modifier.fillMaxSize()) {
            schedules.forEach { schedule ->
                ScheduleItem(
                    schedule = schedule,
                    onLongClick = { onScheduleLongClick(schedule) },
                    onDelete = { onScheduleDelete(schedule) }
                )
            }
        }
    }
}

/**
 * 单个日程项组件
 */
@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleItem(
    schedule: Schedule,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val startTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(schedule.startTime),
        ZoneId.systemDefault()
    ).toLocalTime()
    val endTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(schedule.endTime),
        ZoneId.systemDefault()
    ).toLocalTime()
    
    val startMinutes = startTime.hour * 60 + startTime.minute
    val endMinutes = endTime.hour * 60 + endTime.minute
    val height = (endMinutes - startMinutes).dp
    
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .offset(y = startMinutes.dp)
            .fillMaxWidth()
            .height(height)
            .clickable { /* 点击查看详情 */ }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = schedule.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (schedule.description != null) {
                Text(
                    text = schedule.description,
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = String.format("%02d:%02d - %02d:%02d", 
                    startTime.hour, startTime.minute, 
                    endTime.hour, endTime.minute),
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
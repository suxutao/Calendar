package com.calendar.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calendar.core.utils.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MonthView(
    initialSelectedDate: LocalDate? = null,
    onAddScheduleClick: (LocalDate) -> Unit = {}
) {
    // 核心状态管理
    val today = LocalDate.now()
    val initialDate = initialSelectedDate ?: today
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    val weekTitles = listOf("日", "一", "二", "三", "四", "五", "六")

    // 监听 initialSelectedDate 变化，强制更新月份和选中日期
    LaunchedEffect(initialSelectedDate) {
        if (initialSelectedDate != null) {
            currentMonth = YearMonth.from(initialSelectedDate)
            selectedDate = initialSelectedDate
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentMonth) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            dragTotal += dragAmount.y
                            change.consume()
                        },
                        onDragEnd = {
                            when {
                                dragTotal < -swipeThreshold -> currentMonth =
                                    DateUtils.getNextMonth(currentMonth)
                                dragTotal > swipeThreshold -> currentMonth =
                                    DateUtils.getPreviousMonth(currentMonth)
                            }
                            dragTotal = 0f
                        }
                    )
                }
        ) {
            // 月份标题
            Text(
                text = DateUtils.formatYearMonth(currentMonth),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // 星期标题行
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(weekTitles) { title ->
                    Text(
                        text = title,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        color = if (title == "六" || title == "日") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 日期网格（普通日期无边框，仅今天有边框）
            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    if (targetState.isAfter(initialState)) {
                        slideInVertically(tween(300)) { it } + fadeIn() togetherWith
                                slideOutVertically(tween(300)) { -it } + fadeOut()
                    } else {
                        slideInVertically(tween(300)) { -it } + fadeIn() togetherWith
                                slideOutVertically(tween(300)) { it } + fadeOut()
                    }
                },
                label = "Month transition"
            ) { targetMonth ->
                val dates = DateUtils.getMonthDates(targetMonth)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(dates) { date ->
                        val isToday = DateUtils.isToday(date)
                        val isCurrentMonth = DateUtils.isInCurrentMonth(date, targetMonth)
                        val isWeekend = date.dayOfWeek.value % 7 == 0 || date.dayOfWeek.value == 6
                        val isSelected = date == selectedDate

                        // 卡片底色：选中（含今天）用 primaryContainer，普通日期用 surface
                        val cardColors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                !isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface // 普通日期底色
                            }
                        )

                        // 边框逻辑：仅今天有边框，普通日期（含选中的非今天）无边框
                        val borderModifier = if (isToday) {
                            Modifier.border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium
                            )
                        } else {
                            Modifier // 普通日期无边框
                        }

                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .then(borderModifier), // 仅今天应用边框
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected || isToday) 6.dp else 2.dp,
                                pressedElevation = 8.dp
                            ),
                            colors = cardColors,
                            onClick = { selectedDate = date }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant
                                        isWeekend -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 今天按钮
        FloatingActionButton(
            onClick = {
                currentMonth = DateUtils.getCurrentMonth()
                selectedDate = today
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
            onClick = { onAddScheduleClick(selectedDate) },
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
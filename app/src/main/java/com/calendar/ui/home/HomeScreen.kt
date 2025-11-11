package com.calendar.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.calendar.core.constants.ViewMode
import com.calendar.ui.calendar.DayView
import com.calendar.ui.calendar.MonthView
import com.calendar.ui.calendar.WeekView
import com.calendar.ui.calendar.YearView
import com.calendar.ui.components.CalendarTopBar
import com.calendar.ui.components.DatePickerDialog
import com.calendar.ui.components.ViewModeToggleRow
import com.calendar.ui.components.showToast
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen() {
    // 上下文（用于Toast提示）
    val context = LocalContext.current
    // 本地状态管理当前视图模式（后续可迁移到ViewModel）
    var currentViewMode by remember { mutableStateOf(ViewMode.MONTH) }

    var showDatePicker by remember { mutableStateOf(false) } // 控制弹窗显示
    var targetDate by remember { mutableStateOf(LocalDate.now()) } // 选中的目标日期


    // 主布局容器
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部导航栏
        CalendarTopBar(
            onScheduleClick = { showToast(context, "日程列表（待实现）") },
            onDateJumpClick = { showDatePicker = true },
            onImportExportClick = { showToast(context, "日程导入导出（待实现）") },
            onSettingsClick = { showToast(context, "设置页面（待实现）") }
        )

        // 视图切换按钮行（带边距）
        ViewModeToggleRow(
            currentMode = currentViewMode,
            onModeChanged = { currentViewMode = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 视图内容区域（占满剩余空间）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (currentViewMode) {
                ViewMode.YEAR -> YearView()
                ViewMode.MONTH -> MonthView(
                    initialSelectedDate = targetDate,
                    onAddScheduleClick = { selectedDate ->
                        showToast(context, "添加${selectedDate}的日程（待实现）")
                    }
                )
                ViewMode.WEEK -> WeekView()
                ViewMode.DAY -> DayView()
            }
        }

        // 显示日期选择器弹窗
        DatePickerDialog(
            visible = showDatePicker,
            initialDate = targetDate, // 初始显示当前选中的日期
            onDateSelected = { selectedDate ->
                // 选择日期后，更新目标日期并切换到对应月份
                targetDate = selectedDate
                // 自动切换到月视图（可选，确保用户在月视图中看到跳转结果）
                currentViewMode = ViewMode.MONTH
                showToast(context, "已跳转到${selectedDate}")
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
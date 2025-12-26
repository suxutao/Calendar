package com.calendar.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.calendar.constants.ViewMode
import com.calendar.ui.calendar.DayView
import com.calendar.ui.calendar.MonthView
import com.calendar.ui.calendar.ScheduleView
import com.calendar.ui.calendar.WeekView
import com.calendar.ui.components.CalendarActionBar
import com.calendar.ui.components.CalendarTopBar
import com.calendar.ui.components.DatePickerDialog
import com.calendar.ui.components.showToast
import com.calendar.ui.schedules.AddScheduleScreen
import com.calendar.ui.schedules.EditScheduleScreen
import com.calendar.model.Schedule
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.ui.settings.SettingsScreen
import com.calendar.viewmodel.ScheduleViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentViewMode by remember { mutableStateOf(ViewMode.MONTH) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddScheduleScreen by remember { mutableStateOf(false) }
    var targetDate by remember { mutableStateOf(LocalDate.now()) }
    var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showEditScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    val scheduleViewModel: ScheduleViewModel = viewModel()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CalendarTopBar(
                onDateJumpClick = { showDatePicker = true },
                onSettingsClick = { showSettingsScreen = true }
            )

            CalendarActionBar(
                currentMode = currentViewMode,
                onModeChanged = { currentViewMode = it },
                onScheduleClick = { currentViewMode = ViewMode.SCHEDULE },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                when (currentViewMode) {
                    ViewMode.MONTH -> MonthView(
                        selectedDate = targetDate,
                        onDateSelected = { newDate -> targetDate = newDate },
                        initialSelectedDate = targetDate,
                        onAddScheduleClick = { selectedDate ->
                            targetDate = selectedDate
                            showAddScheduleScreen = true
                        }
                    )
                    ViewMode.WEEK -> WeekView(
                        selectedDate = targetDate,
                        onDateSelected = { targetDate = it },
                        onAddScheduleClick = { selectedDate ->
                            targetDate = selectedDate
                            showAddScheduleScreen = true
                        },
                        onScheduleClick = { schedule ->
                            editingSchedule = schedule
                            showEditScreen = true
                        }
                    )
                    ViewMode.DAY -> DayView(
                        selectedDate = targetDate,
                        onAddScheduleClick = { selectedDate ->
                            targetDate = selectedDate
                            showAddScheduleScreen = true
                        },
                        onScheduleClick = { schedule ->
                            editingSchedule = schedule
                            showEditScreen = true
                        }
                    )
                    ViewMode.SCHEDULE -> ScheduleView(
                        onAddScheduleClick = { selectedDate ->
                            targetDate = selectedDate
                            showAddScheduleScreen = true
                        }
                    )
                }
            }
        }
    }

    DatePickerDialog(
        visible = showDatePicker,
        initialDate = targetDate,
        onDateSelected = { selectedDate ->
            targetDate = selectedDate
            currentViewMode = ViewMode.MONTH
            showToast(context, "已跳转到${selectedDate}")
        },
        onDismiss = { showDatePicker = false }
    )
    
    if (showAddScheduleScreen) {
        AddScheduleScreen(
            selectedDate = targetDate,
            onNavigateBack = { showAddScheduleScreen = false },
            viewModel = scheduleViewModel
        )
    }

    if (showEditScreen && editingSchedule != null) {
        EditScheduleScreen(
            schedule = editingSchedule!!,
            onNavigateBack = {
                showEditScreen = false
                editingSchedule = null
            },
            viewModel = scheduleViewModel
        )
    }

    if (showSettingsScreen) {
        SettingsScreen(
            onNavigateBack = { showSettingsScreen = false }
        )
    }
}

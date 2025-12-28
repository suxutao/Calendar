package com.calendar.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.calendar.db.AppDatabase
import com.calendar.model.Schedule
import com.calendar.repository.ScheduleRepository
import com.calendar.service.ReminderForegroundService
import com.calendar.util.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getInstance(application)
    private val repository = ScheduleRepository(database.scheduleDao())
    
    val allSchedules = repository.allSchedules
    
    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate
    
    init {
    }
    
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO)  {
            repository.add(schedule)
            AlarmScheduler.scheduleReminder(getApplication(), schedule)
            try {
                ReminderForegroundService.start(getApplication())
            } catch (e: Exception) {
            }
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.edit(schedule)
            AlarmScheduler.rescheduleReminder(getApplication(), schedule)
            try {
                ReminderForegroundService.start(getApplication())
            } catch (e: Exception) {
            }
        }
    }
    
    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO) {
            AlarmScheduler.cancelReminder(getApplication(), schedule.id)
            repository.remove(schedule)
        }
    }
    
    fun getSchedulesByDate(date: LocalDate) = viewModelScope.launch(Dispatchers.IO) {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        repository.getDaySchedules(startOfDay, endOfDay)
    }
}

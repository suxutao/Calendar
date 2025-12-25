package com.calendar.viewmodel

import android.annotation.SuppressLint
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
import com.calendar.util.AlarmUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * 日程ViewModel，管理日程数据的业务逻辑
 */
@RequiresApi(Build.VERSION_CODES.O)
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    
    // 获取数据库实例
    private val database = AppDatabase.getInstance(application)
    // 创建Repository实例
    private val repository = ScheduleRepository(database.scheduleDao())
    
    // 所有日程的LiveData
    val allSchedules = repository.allSchedules
    
    // 当前选中的日期
    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate
    
    /**
     * 设置选中的日期
     */
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    /**
     * 添加日程
     */

    @SuppressLint("ScheduleExactAlarm")
    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.add(schedule)
            // 设置闹钟提醒
            val newSchedule = schedule.copy(id = id)
            AlarmUtil.setAlarm(getApplication(), newSchedule)
        }
    }
    
    /**
     * 更新日程
     */
    @SuppressLint("ScheduleExactAlarm")
    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.edit(schedule)
            // 更新闹钟提醒
            AlarmUtil.cancelAlarm(getApplication(), schedule.id)
            AlarmUtil.setAlarm(getApplication(), schedule)
        }
    }
    
    /**
     * 删除日程
     */
    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.remove(schedule)
            // 取消闹钟提醒
            AlarmUtil.cancelAlarm(getApplication(), schedule.id)
        }
    }
    
    /**
     * 获取指定日期的日程
     */
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

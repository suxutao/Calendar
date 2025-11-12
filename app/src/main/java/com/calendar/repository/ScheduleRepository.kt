package com.calendar.repository

import com.calendar.db.ScheduleDao
import com.calendar.model.Schedule
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {

    // 观察所有日程（数据变化时自动通知UI）
    val allSchedules: Flow<List<Schedule>> = dao.getAll()

    // 新增日程
    suspend fun add(schedule: Schedule) = dao.insert(schedule)

    // 更新日程
    suspend fun edit(schedule: Schedule) = dao.update(schedule)

    // 删除日程
    suspend fun remove(schedule: Schedule) = dao.delete(schedule)

    // 根据ID查询日程
    suspend fun getSchedule(id: Long): Schedule? = dao.getById(id)

    // 查询某天的日程（传入当天0点和次日0点的时间戳）
    suspend fun getDaySchedules(dayStart: Long, dayEnd: Long): List<Schedule> {
        return dao.getInRange(dayStart, dayEnd)
    }
}
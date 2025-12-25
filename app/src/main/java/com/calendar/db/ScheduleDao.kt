package com.calendar.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.calendar.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: Schedule): Long

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Long): Schedule?

    // 按创建时间倒序查询所有日程
    @Query("SELECT * FROM schedules ORDER BY createTime DESC")
    fun getAll(): Flow<List<Schedule>>

    // 按时间范围查询（如某天的日程）
    @Query("SELECT * FROM schedules WHERE startTime < :end AND endTime > :start ORDER BY startTime ASC")
    suspend fun getInRange(start: Long, end: Long): List<Schedule>
    
    // 同步查询所有日程（用于后台服务）
    @Query("SELECT * FROM schedules ORDER BY createTime DESC")
    fun getAllSchedulesSync(): List<Schedule>
}
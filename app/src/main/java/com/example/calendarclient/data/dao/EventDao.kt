package com.example.calendarclient.data.dao

import androidx.room.*
import com.example.calendarclient.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM events WHERE uid = :uid")
    suspend fun getEventById(uid: String): CalendarEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("SELECT * FROM events WHERE (dtStart >= :start AND dtStart < :end) OR (dtEnd > :start AND dtEnd <= :end)")
    fun getEventsInRange(start: Long, end: Long): Flow<List<CalendarEvent>>
}

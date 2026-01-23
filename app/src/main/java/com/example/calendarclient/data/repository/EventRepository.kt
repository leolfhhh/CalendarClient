package com.example.calendarclient.data.repository

import com.example.calendarclient.data.dao.EventDao
import com.example.calendarclient.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<CalendarEvent>> = eventDao.getAllEvents()

    fun getEventsInRange(start: Long, end: Long): Flow<List<CalendarEvent>> =
        eventDao.getEventsInRange(start, end)

    suspend fun getEventById(uid: String): CalendarEvent? = eventDao.getEventById(uid)

    suspend fun insertEvent(event: CalendarEvent) = eventDao.insertEvent(event)

    suspend fun updateEvent(event: CalendarEvent) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: CalendarEvent) = eventDao.deleteEvent(event)
}

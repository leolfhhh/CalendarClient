package com.example.calendarclient.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "events")
data class CalendarEvent(
    @PrimaryKey val uid: String = UUID.randomUUID().toString(),
    val summary: String,
    val description: String? = null,
    val dtStart: Long, // Epoch milliseconds
    val dtEnd: Long,   // Epoch milliseconds
    val location: String? = null,
    val rrule: String? = null,
    val reminderTime: Long? = null, // Minutes before event
    val isAllDay: Boolean = false
)

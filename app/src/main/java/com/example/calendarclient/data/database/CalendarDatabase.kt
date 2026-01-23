package com.example.calendarclient.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.calendarclient.data.dao.EventDao
import com.example.calendarclient.data.model.CalendarEvent

@Database(entities = [CalendarEvent::class], version = 1, exportSchema = false)
abstract class CalendarDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}

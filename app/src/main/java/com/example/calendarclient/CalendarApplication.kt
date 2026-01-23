package com.example.calendarclient

import android.app.Application
import androidx.room.Room
import com.example.calendarclient.data.database.CalendarDatabase
import com.example.calendarclient.data.repository.EventRepository

class CalendarApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            CalendarDatabase::class.java,
            "calendar_database"
        ).build()
    }
    val repository by lazy { EventRepository(database.eventDao()) }
}

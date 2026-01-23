package com.example.calendarclient.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.calendarclient.data.model.CalendarEvent

class ReminderManager(private val context: Context) {
    fun scheduleReminder(event: CalendarEvent) {
        if (event.reminderTime == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", event.summary)
            putExtra("message", "Event at ${event.location ?: "scheduled time"}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.uid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = event.dtStart - (event.reminderTime * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    fun cancelReminder(event: CalendarEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.uid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

package com.example.calendarclient.service

import biweekly.Biweekly
import biweekly.ICalendar
import biweekly.component.VEvent
import com.example.calendarclient.data.model.CalendarEvent
import java.io.InputStream
import java.util.*

class IcsManager {
    fun exportToIcs(events: List<CalendarEvent>): String {
        val ical = ICalendar()
        for (event in events) {
            val vEvent = VEvent()
            vEvent.setUid(event.uid)
            vEvent.setSummary(event.summary)
            vEvent.setDescription(event.description)
            vEvent.setLocation(event.location)
            vEvent.setDateStart(Date(event.dtStart))
            vEvent.setDateEnd(Date(event.dtEnd))
            ical.addEvent(vEvent)
        }
        return Biweekly.write(ical).go()
    }

    fun importFromIcs(inputStream: InputStream): List<CalendarEvent> {
        val ical = Biweekly.parse(inputStream).first() ?: return emptyList()
        return ical.events.map { vEvent ->
            CalendarEvent(
                uid = vEvent.uid?.value ?: UUID.randomUUID().toString(),
                summary = vEvent.summary?.value ?: "No Title",
                description = vEvent.description?.value,
                location = vEvent.location?.value,
                dtStart = vEvent.dateStart?.value?.time ?: System.currentTimeMillis(),
                dtEnd = vEvent.dateEnd?.value?.time ?: (vEvent.dateStart?.value?.time?.plus(3600000) ?: System.currentTimeMillis())
            )
        }
    }
}

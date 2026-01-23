package com.example.calendarclient.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calendarclient.data.model.CalendarEvent
import com.example.calendarclient.data.repository.EventRepository
import com.example.calendarclient.service.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class EventEditViewModel(
    private val repository: EventRepository,
    private val reminderManager: ReminderManager,
    private val eventId: String?,
    initialDate: LocalDate?
) : ViewModel() {
    private val _summary = MutableStateFlow("")
    val summary = _summary.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _date = MutableStateFlow(initialDate ?: LocalDate.now())
    val date = _date.asStateFlow()

    private val _startTime = MutableStateFlow(LocalTime.now())
    val startTime = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow(LocalTime.now().plusHours(1))
    val endTime = _endTime.asStateFlow()

    private val _reminderMinutes = MutableStateFlow<Long?>(10) // Default 10 mins
    val reminderMinutes = _reminderMinutes.asStateFlow()

    init {
        if (eventId != null) {
            viewModelScope.launch {
                repository.getEventById(eventId)?.let { event ->
                    _summary.value = event.summary
                    _description.value = event.description ?: ""
                    _location.value = event.location ?: ""
                    val startDateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(event.dtStart),
                        ZoneId.systemDefault()
                    )
                    _date.value = startDateTime.toLocalDate()
                    _startTime.value = startDateTime.toLocalTime()
                    val endDateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(event.dtEnd),
                        ZoneId.systemDefault()
                    )
                    _endTime.value = endDateTime.toLocalTime()
                    _reminderMinutes.value = event.reminderTime
                }
            }
        }
    }

    fun updateSummary(value: String) { _summary.value = value }
    fun updateDescription(value: String) { _description.value = value }
    fun updateLocation(value: String) { _location.value = value }
    fun updateDate(value: LocalDate) { _date.value = value }
    fun updateStartTime(value: LocalTime) { _startTime.value = value }
    fun updateEndTime(value: LocalTime) { _endTime.value = value }
    fun updateReminder(value: Long?) { _reminderMinutes.value = value }

    fun saveEvent(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val startDateTime = LocalDateTime.of(_date.value, _startTime.value)
            val endDateTime = LocalDateTime.of(_date.value, _endTime.value)
            
            val event = CalendarEvent(
                uid = eventId ?: java.util.UUID.randomUUID().toString(),
                summary = _summary.value,
                description = _description.value.takeIf { it.isNotEmpty() },
                location = _location.value.takeIf { it.isNotEmpty() },
                dtStart = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                dtEnd = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                reminderTime = _reminderMinutes.value
            )
            
            if (eventId == null) {
                repository.insertEvent(event)
            } else {
                repository.updateEvent(event)
            }
            
            if (event.reminderTime != null) {
                reminderManager.scheduleReminder(event)
            } else {
                reminderManager.cancelReminder(event)
            }
            
            onSuccess()
        }
    }

    fun deleteEvent(onSuccess: () -> Unit) {
        if (eventId != null) {
            viewModelScope.launch {
                repository.getEventById(eventId)?.let {
                    reminderManager.cancelReminder(it)
                    repository.deleteEvent(it)
                    onSuccess()
                }
            }
        }
    }

    class Factory(
        private val repository: EventRepository,
        private val reminderManager: ReminderManager,
        private val eventId: String?,
        private val initialDate: LocalDate?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventEditViewModel(repository, reminderManager, eventId, initialDate) as T
        }
    }
}

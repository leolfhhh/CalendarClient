package com.example.calendarclient.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calendarclient.api.AIAgentService
import com.example.calendarclient.api.AIRequest
import com.example.calendarclient.api.AIResponse
import com.example.calendarclient.data.model.CalendarEvent
import com.example.calendarclient.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

enum class CalendarViewMode {
    MONTH, WEEK, DAY
}

class CalendarViewModel(private val repository: EventRepository) : ViewModel() {
    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _isProcessingAI = MutableStateFlow(false)
    val isProcessingAI = _isProcessingAI.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            repository.allEvents.collect {
                _events.value = it
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        repository.deleteEvent(event)
    }

    fun processAIQuery(query: String, onResult: (AIResponse) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isProcessingAI.value = true
            try {
                // In a real app, this would call the API. For now, it's a hook.
                // val response = AIAgentService.api.processQuery(AIRequest(query))
                // onResult(response)
                
                // Mock result for demonstration if API fails/not set
                onError("AI Backend not configured. Please set BASE_URL in AIAgentService.")
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            } finally {
                _isProcessingAI.value = false
            }
        }
    }

    fun importEvents(events: List<CalendarEvent>) {
        viewModelScope.launch {
            events.forEach { repository.insertEvent(it) }
        }
    }

    class Factory(private val repository: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository) as T
        }
    }
}

package com.example.calendarclient.ui.calendar

import android.util.Log
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
            // #region agent log
            Log.d("DEBUG_AI", "[H1,H2,H4,H5] processAIQuery called | query=$query | isProcessingAI=${_isProcessingAI.value}")
            // #endregion
            _isProcessingAI.value = true
            try {
                // #region agent log
                Log.d("DEBUG_AI", "[H1] About to make API call | query=$query")
                // #endregion
                val response = AIAgentService.api.processQuery(AIRequest(query))
                // #region agent log
                Log.d("DEBUG_AI", "[H1] API call successful | response=$response")
                // #endregion
                onResult(response)
            } catch (e: Exception) {
                // #region agent log
                Log.e("DEBUG_AI", "[H4] Exception caught | exception=${e.javaClass.simpleName} | message=${e.message}", e)
                Log.e("DEBUG_AI", "[H4] Full stack trace:", e)
                e.cause?.let { cause ->
                    Log.e("DEBUG_AI", "[H4] Caused by: ${cause.javaClass.simpleName} | message=${cause.message}")
                }
                // #endregion
                onError(e.message ?: "Unknown error")
            } finally {
                // #region agent log
                Log.d("DEBUG_AI", "[H1,H2,H4] processAIQuery completed | isProcessingAI=${_isProcessingAI.value}")
                // #endregion
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

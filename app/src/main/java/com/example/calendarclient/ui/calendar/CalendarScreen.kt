package com.example.calendarclient.ui.calendar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendarclient.api.AIResponse
import com.example.calendarclient.data.model.CalendarEvent
import com.example.calendarclient.service.IcsManager
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onAddEvent: (LocalDate) -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
    onSmartAddResult: (AIResponse) -> Unit
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val events by viewModel.events.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val isProcessingAI by viewModel.isProcessingAI.collectAsState()

    var showSmartAddDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val importedEvents = IcsManager().importFromIcs(stream)
                viewModel.importEvents(importedEvents)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/calendar")) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                val icsData = IcsManager().exportToIcs(events)
                stream.write(icsData.toByteArray())
            }
        }
    }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atDay(1),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = LocalDate.now(),
        firstDayOfWeek = firstDayOfWeek
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                actions = {
                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Import ICS")
                    }
                    IconButton(onClick = { exportLauncher.launch("calendar.ics") }) {
                        Icon(Icons.Default.Share, contentDescription = "Export ICS")
                    }
                    IconButton(onClick = { viewModel.setViewMode(CalendarViewMode.MONTH) }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Month View")
                    }
                    IconButton(onClick = { viewModel.setViewMode(CalendarViewMode.WEEK) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Week View")
                    }
                    IconButton(onClick = { viewModel.setViewMode(CalendarViewMode.DAY) }) {
                        Icon(Icons.Default.List, contentDescription = "Day View")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { showSmartAddDialog = true },
                    modifier = Modifier.padding(bottom = 8.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Face, contentDescription = "Smart Add")
                }
                FloatingActionButton(onClick = { onAddEvent(selectedDate) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (viewMode) {
                CalendarViewMode.MONTH -> {
                    HorizontalCalendar(
                        state = monthState,
                        dayContent = { day ->
                            val hasEvent = events.any { isEventOnDate(it, day.date) }
                            Day(
                                day = day.date,
                                isSelected = selectedDate == day.date,
                                hasEvent = hasEvent,
                                isCurrentMonth = day.position == DayPosition.MonthDate,
                                onClick = { viewModel.selectDate(it) }
                            )
                        },
                        monthHeader = { month ->
                            val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
                            MonthHeader(daysOfWeek = daysOfWeek, month = month.yearMonth)
                        }
                    )
                }
                CalendarViewMode.WEEK -> {
                    WeekCalendar(
                        state = weekState,
                        dayContent = { day ->
                            val hasEvent = events.any { isEventOnDate(it, day.date) }
                            Day(
                                day = day.date,
                                isSelected = selectedDate == day.date,
                                hasEvent = hasEvent,
                                isCurrentMonth = true,
                                onClick = { viewModel.selectDate(it) }
                            )
                        }
                    )
                }
                CalendarViewMode.DAY -> {
                    WeekCalendar(
                        state = weekState,
                        dayContent = { day ->
                            val hasEvent = events.any { isEventOnDate(it, day.date) }
                            Day(
                                day = day.date,
                                isSelected = selectedDate == day.date,
                                hasEvent = hasEvent,
                                isCurrentMonth = true,
                                onClick = { viewModel.selectDate(it) }
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            val selectedDateEvents = remember(selectedDate, events) {
                events.filter { isEventOnDate(it, selectedDate) }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = selectedDate.toString(),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (selectedDateEvents.isEmpty()) {
                    item {
                        Text(
                            text = "No events for this day",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    items(selectedDateEvents) { event ->
                        EventItem(event = event, onClick = { onEditEvent(event) })
                    }
                }
            }
        }
    }

    if (showSmartAddDialog) {
        var query by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSmartAddDialog = false },
            title = { Text("Smart Add") },
            text = {
                Column {
                    Text("Enter natural language (e.g., 'Meeting with Bob tomorrow at 2pm')")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessingAI
                    )
                    if (isProcessingAI) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.processAIQuery(
                            query,
                            onResult = {
                                showSmartAddDialog = false
                                onSmartAddResult(it)
                            },
                            onError = { /* Handle error */ }
                        )
                    },
                    enabled = query.isNotEmpty() && !isProcessingAI
                ) { Text("Process") }
            },
            dismissButton = {
                TextButton(onClick = { showSmartAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

fun isEventOnDate(event: CalendarEvent, date: LocalDate): Boolean {
    val eventDate = Instant.ofEpochMilli(event.dtStart)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return eventDate == date
}

@Composable
fun Day(
    day: LocalDate,
    isSelected: Boolean,
    hasEvent: Boolean,
    isCurrentMonth: Boolean,
    onClick: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.dayOfMonth.toString(),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else if (isCurrentMonth) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                },
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasEvent) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun MonthHeader(daysOfWeek: List<java.time.DayOfWeek>, month: YearMonth) {
    Column {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EventItem(event: CalendarEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.summary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            event.location?.let {
                if (it.isNotEmpty()) {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
            val startTime = Instant.ofEpochMilli(event.dtStart).atZone(ZoneId.systemDefault()).toLocalTime()
            val endTime = Instant.ofEpochMilli(event.dtEnd).atZone(ZoneId.systemDefault()).toLocalTime()
            Text(text = "$startTime - $endTime", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

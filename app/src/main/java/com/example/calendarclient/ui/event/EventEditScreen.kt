package com.example.calendarclient.ui.event

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    viewModel: EventEditViewModel,
    onBack: () -> Unit
) {
    val summary by viewModel.summary.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val date by viewModel.date.collectAsState()
    val startTime by viewModel.startTime.collectAsState()
    val endTime by viewModel.endTime.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteEvent(onBack) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = { viewModel.saveEvent(onBack) }) {
                        Icon(Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = summary,
                onValueChange = { viewModel.updateSummary(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location,
                onValueChange = { viewModel.updateLocation(it) },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val reminderMinutes by viewModel.reminderMinutes.collectAsState()
                Text("Reminder: ${reminderMinutes ?: "None"}")
                Slider(
                    value = (reminderMinutes ?: 0L).toFloat(),
                    onValueChange = { viewModel.updateReminder(it.toLong().takeIf { it > 0 }) },
                    valueRange = 0f..60f,
                    steps = 12
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Start: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
                Button(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "End: ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.updateDate(
                            java.time.Instant.ofEpochMilli(it)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startTime.hour,
            initialMinute = startTime.minute
        )
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateStartTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showStartTimePicker = false
                }) { Text("OK") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endTime.hour,
            initialMinute = endTime.minute
        )
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateEndTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showEndTimePicker = false
                }) { Text("OK") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        text = { content() }
    )
}

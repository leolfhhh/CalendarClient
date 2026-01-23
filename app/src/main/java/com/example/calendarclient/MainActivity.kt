package com.example.calendarclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calendarclient.ui.calendar.CalendarScreen
import com.example.calendarclient.ui.calendar.CalendarViewModel
import com.example.calendarclient.ui.event.EventEditScreen
import com.example.calendarclient.ui.event.EventEditViewModel
import com.example.calendarclient.service.ReminderManager
import com.example.calendarclient.ui.theme.CalendarClientTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalendarClientTheme {
                CalendarApp()
            }
        }
    }
}

@Composable
fun CalendarApp() {
    val context = LocalContext.current
    val application = context.applicationContext as CalendarApplication
    val repository = application.repository
    val reminderManager = remember { ReminderManager(context) }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "calendar") {
        composable("calendar") {
            val calendarViewModel: CalendarViewModel = viewModel(
                factory = CalendarViewModel.Factory(repository)
            )
            CalendarScreen(
                viewModel = calendarViewModel,
                onAddEvent = { date ->
                    navController.navigate("editEvent?date=${date}")
                },
                onEditEvent = { event ->
                    navController.navigate("editEvent?eventId=${event.uid}")
                },
                onSmartAddResult = { response ->
                    val start = LocalDateTime.parse(response.dtStart).toLocalDate()
                    navController.navigate("editEvent?summary=${response.summary}&description=${response.description ?: ""}&location=${response.location ?: ""}&date=${start}&startTime=${LocalDateTime.parse(response.dtStart).toLocalTime()}&endTime=${LocalDateTime.parse(response.dtEnd).toLocalTime()}")
                }
            )
        }
        composable(
            route = "editEvent?eventId={eventId}&date={date}&summary={summary}&description={description}&location={location}&startTime={startTime}&endTime={endTime}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("date") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("summary") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("description") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("location") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("startTime") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("endTime") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val dateStr = backStackEntry.arguments?.getString("date")
            val date = dateStr?.let { LocalDate.parse(it) }
            
            val summary = backStackEntry.arguments?.getString("summary")
            val description = backStackEntry.arguments?.getString("description")
            val location = backStackEntry.arguments?.getString("location")
            val startTimeStr = backStackEntry.arguments?.getString("startTime")
            val endTimeStr = backStackEntry.arguments?.getString("endTime")

            val editViewModel: EventEditViewModel = viewModel(
                factory = EventEditViewModel.Factory(repository, reminderManager, eventId, date)
            )
            
            // Populate AI results if present
            LaunchedEffect(summary) {
                summary?.let { editViewModel.updateSummary(it) }
                description?.let { editViewModel.updateDescription(it) }
                location?.let { editViewModel.updateLocation(it) }
                startTimeStr?.let { editViewModel.updateStartTime(java.time.LocalTime.parse(it)) }
                endTimeStr?.let { editViewModel.updateEndTime(java.time.LocalTime.parse(it)) }
            }

            EventEditScreen(
                viewModel = editViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

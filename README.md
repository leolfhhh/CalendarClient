# CalendarClient

CalendarClient is a modern Android calendar application built with Jetpack Compose. It features AI-powered event creation, iCalendar (ICS) support, and robust event management capabilities.

## Features

- **Event Management**: Create, view, and edit calendar events with support for summaries, descriptions, locations, and time ranges.
- **AI Smart Add**: Use natural language to quickly create events. The app communicates with an AI backend to parse details like dates, times, and descriptions from your queries.
- **iCalendar Support**: Export your events to `.ics` files or import existing ones using the integrated `biweekly` library.
- **Local Persistence**: All your calendar data is stored securely on-device using a Room database.
- **Reminders**: Never miss an event with local notification support.
- **Modern UI**: A clean and responsive interface built entirely with Jetpack Compose and Material Design 3.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room
- **Networking**: Retrofit & Gson
- **Calendar Parsing**: [biweekly](https://github.com/mangstadt/biweekly)
- **Navigation**: Jetpack Navigation Compose
- **Architecture**: MVVM (Model-View-ViewModel)

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 31+ (Min SDK 31, Target SDK 36)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/CalendarApp.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or physical device.

### AI Backend Configuration

The "Smart Add" feature requires an AI backend service. You can configure the backend URL in:
`app/src/main/java/com/example/calendarclient/api/AIAgentService.kt`

```kotlin
private const val BASE_URL = "http://10.0.2.2:8000/process"
```

## Project Structure

- `app/src/main/java/com/example/calendarclient/api`: AI service and networking.
- `app/src/main/java/com/example/calendarclient/data`: Room database entities, DAOs, and repositories.
- `app/src/main/java/com/example/calendarclient/service`: Core logic for ICS management and reminders.
- `app/src/main/java/com/example/calendarclient/ui`: Compose screens, ViewModels, and themes.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

# SpeechMaster Android Application

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Introduction

SpeechMaster is an Android mobile application designed to help users improve their language speaking skills (initially focusing on English) through structured daily practice. The app provides practice content, recording capabilities, intelligent feedback on pronunciation, fluency, and integrity, and progress tracking to make language learning more efficient and engaging. The current version emphasizes a local-first experience, storing all user data and progress directly on the device.

## Key Features

* **Home Dashboard:** Overview of progress, featured courses, and quick access to continue ongoing learning.
* **Course Library:** Browse, search, and filter built-in and user-created courses.
* **Course Details:** View course information, see all practice cards, and add/remove courses from "My Learning".
* **Interactive Practice Screen:**
    * Display practice text clearly.
    * Record user's speech.
    * Provide reference audio using Android's native Text-to-Speech (TTS).
    * Controls for playback, re-recording, and submitting for analysis.
* **Detailed Feedback Screen:**
    * Overall score and qualitative rating.
    * Dimensional analysis: Pronunciation Accuracy, Fluency, Integrity.
    * Highlighting of mispronounced words within the original text.
    * Playback of the user's recording.
    * Options to retry the card or mark as complete.
* **My Learning:** Manage courses added by the user, track progress (completed cards), and easily continue practice sessions.
* **My Courses (UGC):** Create, edit, and manage personal practice courses and cards.
* **Offline-First:** All user progress, created content, and settings are stored locally on the device (Note: Uninstalling the app will remove all data).
* **Settings:** Configure application preferences (Theme, Notifications, API Keys).
* **About:** Application information, version, links to policies, etc.

## Screenshots / Demo

<p align="center">
  </p>

## Technology Stack

* **Language:** Kotlin (Primary)
* **UI:** Jetpack Compose with Material 3
* **Architecture:** Clean Architecture (Data -> Domain -> UI), MVVM on UI Layer
* **Asynchronous Programming:** Kotlin Coroutines & Flow
* **Dependency Injection:** Hilt
* **Database:** Room Persistence Library (SQLite)
* **Settings Persistence:** Jetpack DataStore (Preferences)
* **Navigation:** Jetpack Navigation Component (Compose Navigation)
* **Networking:** Retrofit & OkHttp (for Speech Analysis API)
* **Speech Recording:** `android.media.AudioRecord` (via `WavAudioRecorder` wrapper)
* **Speech Playback:** `android.media.MediaPlayer` (via `AudioPlayerWrapper`)
* **Text-to-Speech:** Android Native TTS (`android.speech.tts.TextToSpeech` via `TextToSpeechWrapper`)
* **Speech Analysis:** Microsoft Azure Speech SDK (via `SpeechAnalyzerWrapper`) - **Requires API Key**
* **Background Processing:** Android WorkManager (`SpeechAnalysisWorker`)

## Architecture Overview

SpeechMaster follows the principles of Clean Architecture, promoting separation of concerns, testability, and maintainability.

* **Data Layer:** Contains data sources (Room database, Azure Speech API), Repositories implementations, data models (Entities, DTOs), and mappers. Responsible for data retrieval, storage, and transformation.
* **Domain Layer:** Defines business logic, use cases (optional for complex logic), repository interfaces (`IXxxRepository`), and domain models (simple data classes). It acts as the core business rule layer, independent of Android frameworks.
* **UI Layer:** Implements the user interface using Jetpack Compose. It follows the MVVM pattern, utilizing ViewModels (`XxxViewModel`), UI State (`XxxUiState` exposed via `StateFlow`), and Composable functions (Screens, Components). User interactions trigger ViewModel methods, which update the state observed by the UI.

Dependency Injection is managed by Hilt throughout the application. Asynchronous operations rely heavily on Kotlin Coroutines and Flow, particularly for database observation and API calls.

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd SpeechMaster
    ```
2.  **Open in Android Studio:** Ensure you have the latest stable version of Android Studio (Hedgehog or newer recommended).
3.  **API Keys:** The core speech analysis feature requires API credentials for the Microsoft Azure Speech Service.
    * Obtain your Speech Service **Key** and **Region**.
    * You can add these keys to your `local.properties` file (ensure this file is in `.gitignore`):
        ```properties
        # local.properties
        MICROSOFT_SPEECH_KEY="YOUR_AZURE_SPEECH_API_KEY"
        MICROSOFT_SPEECH_REGION="YOUR_AZURE_SPEECH_REGION"
        ```
    * Alternatively, users can override these via the in-app Settings screen. The app prioritizes user-set keys over build config keys.
4.  **Build and Run:** Sync the project with Gradle files and run the application on an emulator or physical device (Android 6.0+).

## Development Workflow

The project adheres to specific development practices to ensure code quality and consistency:

* **Version Control:** Follows a standard Git flow (e.g., Gitflow or GitHub Flow). Commit messages should adhere to Conventional Commits format.
* **Task Management:** Features are broken down into smaller tasks tracked using tools like Jira or Trello.
* **Code Reviews:** Mandatory for all code merged into the main branch.
* **Coding Standards:** Strictly follows the official Kotlin coding conventions and Clean Code principles (SOLID, Separation of Concerns). 
* **Architecture:** Adheres to the Clean Architecture pattern described above.
* **Testing:** Includes Unit Tests (ViewModels, Repositories, UseCases), Integration Tests (Room DAO, Migrations), and recommended UI Tests (Compose Testing API) for critical user flows.
* **Database Migrations:** Follows a defined workflow involving schema design updates, Room entity/DAO changes, implementing `Migration` objects, and writing migration tests.

## Roadmap

### Current Focus (v1.2 - Implemented/In Progress)

* Core practice loop: Selecting a card, recording speech, TTS reference audio.
* Azure-based speech analysis providing feedback on pronunciation, fluency, and integrity.
* Displaying detailed feedback with word-level highlighting.
* Local database storage for courses, cards, and practice history using Room.
* User-Generated Content (UGC): Creating, editing, deleting custom courses and cards.
* "My Learning" feature to track progress within added courses.
* Home screen dashboard showing learning progress and featured courses.
* Settings screen for basic configurations (Theme, Notifications, API Key Override).
* Offline-first design for user data.

### Future Considerations

Based on my experience, potential future enhancements include:

* **User Accounts:** Implement user registration and login.
* **Cloud Synchronization:** Sync user progress, UGC courses, and settings across devices.
* **Backend CMS:** Allow updating built-in course content without requiring an app update.
* **Advanced Feedback:** Incorporate analysis for intonation, stress, and pauses.
* **Offline Evaluation:** Explore on-device speech evaluation models (technically challenging).
* **Social Features:** Options for sharing progress or comparing with others (leaderboards).
* **Gamification:** Introduce achievements, points, or streaks to motivate users.
* **Multi-language Support:** Extend practice and feedback to other languages.
* **Content Import:** Allow users to create practice cards by importing external text/files.
* **Smart Recommendations:** Suggest courses based on user level and practice history.
* **Premium Features/Subscription:** Introduce a paid tier for advanced features or exclusive content.


## Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Programming Language](https://kotlinlang.org/)
- All contributors who have helped this project grow

# Tasky

A modern task manager for Android built with Kotlin and Jetpack Compose (Material 3).

Tasky provides a simple interface for creating, organizing, searching, and managing everyday tasks. Tasks are stored locally on the device using Room, allowing the app to work offline.

https://github.com/user-attachments/assets/997a1a8a-c3dc-4c04-b8c3-6d18ef5f305c

## Features

- Add, edit, complete, and delete tasks
- Priority levels: Low / Medium / High
- Categories: Personal, Work, Study, Health
- Due dates with a Material date picker
- Overdue task highlighting
- Smart filters: All, Today, Upcoming, Overdue, Done
- Live search across task titles, notes, and categories
- Progress summary showing completed tasks and completion percentage
- Task completion with checkboxes
- Offline local storage with Room
- Light and dark themes
- Material 3 user interface

## How It Works

1. Create a new task from the main screen.
2. Enter a title and optional notes.
3. Select a priority and category.
4. Set a due date if needed.
5. Save the task.
6. Manage tasks from the main screen using search, filters, completion, editing, and deletion.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room Database
- Android Architecture Components
- ViewModel
- StateFlow
- MVVM architecture

## Architecture

    Tasky UI
       ↓
    TaskViewModel
       ↓
    TaskDao
       ↓
    Room Database
       ↓
    Local Device Storage

## Requirements

- Android 8.0 (API 26) or newer
- Android Studio
- Kotlin
- Gradle

## Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync the Gradle files.
4. Run the app on an Android emulator or physical device running API 26 or newer.

## Release

A debug APK is available from the project's GitHub Releases page for demonstration and coursework purposes.

## Project

**Tasky — Android Task Manager**

Developed by **Shuvo Sarkar** as coursework for **Mobile Application Development**.

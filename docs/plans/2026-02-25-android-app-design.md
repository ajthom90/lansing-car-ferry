# Android App Design

## Goal

Build the Jetpack Compose frontend for the Lansing Car Ferry Android app, achieving feature parity with the existing iOS/SwiftUI app. The shared KMP module (models, repository, networking, caching) is already complete.

## Architecture

Single-activity Compose app. The shared KMP module provides all business logic via `FerryRepository`. The Android layer is UI-only.

- **UI**: Jetpack Compose with Material 3
- **ViewModel**: AndroidX ViewModel exposing `StateFlow<UiState>`
- **Video**: AndroidX Media3 ExoPlayer for HLS (.m3u8) streams
- **Navigation**: Compose Navigation with bottom `NavigationBar` (4 tabs)
- **Images**: Coil for async thumbnail loading
- **Min SDK**: 26 (Android 8.0)

## Theme

Material 3 with custom blue `ColorScheme`:
- Primary: `#1A8FE3`
- Secondary/container: `#0B5FA5`
- Light theme only (matches iOS)

## Screens

1. **Home** — Service note banner, 3 quick-info cards (crossing time, capacity, "FREE"), 2 location rows with "Open in Maps" intents, 3 resource links via `ACTION_VIEW`
2. **Live Cameras** — LazyColumn of 3 cameras, Coil snapshot thumbnails (16:9), tap navigates to ExoPlayer fullscreen
3. **Info** — Schedule with AM/PM formatting, expandable allowed/prohibited vehicle lists, size limits
4. **FAQ** — Expandable question/answer cards

## Data Flow

`FerryRepository.create()` -> `FerryViewModel` collects `FerryResult` as `StateFlow<UiState>` -> Compose screens observe. Pull-to-refresh calls `repository.refresh()`.

## Dependencies

- Jetpack Compose BOM + Material 3
- Compose Navigation
- AndroidX ViewModel + Lifecycle
- Coil Compose (image loading)
- AndroidX Media3 ExoPlayer (HLS)
- KotlinX Coroutines Android

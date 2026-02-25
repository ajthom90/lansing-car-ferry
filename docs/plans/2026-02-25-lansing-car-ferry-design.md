# Lansing Car Ferry App — Design Document

**Date:** 2026-02-25
**Status:** Approved

## Overview

A mobile app (iOS + Android) for the Lansing Car Ferry service operating between Iowa and Wisconsin during the Mississippi River bridge reconstruction. The app provides ferry schedule info, FAQs, vehicle restrictions, and live camera streams. iOS is the primary development target; Android follows once SDK is available.

## Architecture

**Kotlin Multiplatform (KMP)** with native UI on each platform:

- **Shared module (Kotlin):** Data models, repository, networking (Ktor), JSON parsing (kotlinx.serialization), persistent caching (Okio)
- **iOS (SwiftUI):** Native UI targeting iOS 17+, AVPlayer for HLS streams
- **Android (Jetpack Compose):** Native UI (deferred), ExoPlayer for HLS streams

## Project Structure

```
lansing-car-ferry/
├── shared/                          # KMP shared module
│   └── src/
│       ├── commonMain/kotlin/com/lansingferry/shared/
│       │   ├── model/               # Data classes
│       │   ├── repository/          # FerryRepository
│       │   ├── network/             # Ktor HTTP client
│       │   └── cache/               # Persistent + in-memory cache
│       ├── iosMain/                 # Darwin Ktor engine + file path
│       └── androidMain/             # OkHttp Ktor engine + file path
├── iosApp/                          # SwiftUI iOS application
│   └── LansingFerry/
│       ├── App/
│       ├── Views/
│       ├── ViewModels/
│       └── Player/
├── androidApp/                      # Jetpack Compose (deferred)
├── data/                            # JSON for GitHub Pages
│   └── ferry-info.json
├── build.gradle.kts
└── settings.gradle.kts
```

## Data Model

Remote JSON hosted on GitHub Pages. Single `ferry-info.json` containing:

- **schedule:** Regular hours (WI: 5:30am-9pm, IA: 5:45am-9:15pm), holiday hours (7am-7pm), commuter priority windows, holiday dates, crossing duration (15 min), capacity (~12 vehicles)
- **locations:** Iowa (Lansing Marina) and Wisconsin (WIS 82 Landing) with coordinates
- **cameras:** 3 live HLS streams — Wisconsin Ferry Landing, Iowa Ferry Landing, Iowa Ferry Queue — each with stream URL and snapshot URL
- **vehicleRestrictions:** Allowed/prohibited vehicles, size limits (11ft H, 25ft L, 8'6" W, 10 tons)
- **faqs:** Expandable Q&A list (cost, winter ops, crossing time, detour, photos, safety)
- **contact:** Pete Hjelmstad, pete.hjelmstad@iowadot.us
- **links:** Facebook, 511ia.org, Iowa DOT page

### Shared Kotlin Data Classes

- `FerryInfo` (root)
- `Schedule`, `HoursWindow`, `TimeRange`
- `Location`
- `Camera`
- `VehicleRestrictions`, `SizeLimits`
- `FAQ`
- `Contact`
- `Links`

All annotated with `@Serializable` for kotlinx.serialization.

## Shared KMP Layer

### FerryRepository

Single entry point consumed by both platforms:

- `fetchFerryInfo()` — Returns best available data (fresh > stale-cached > error)
- `refreshFerryInfo()` — Forces a network fetch, persists on success

### Caching Strategy: Stale-While-Revalidate with Persistent Fallback

Cell signal is spotty in the ferry area, so offline support is critical.

1. App launches -> load from persistent storage immediately (instant, works offline)
2. If data is older than 30 min and network is available -> fetch fresh data in background
3. On successful fetch -> update in-memory cache AND persistent storage
4. On failed fetch -> keep using whatever was last persisted, no error shown to user
5. Only show an error on true cold start with zero cached data AND no network

### Persistent Storage

- Serialize `FerryInfo` to JSON string via kotlinx.serialization
- Read/write JSON file using Okio (KMP-compatible filesystem)
- Platform-specific `expect`/`actual` for file path (`NSDocumentDirectory` on iOS, `context.filesDir` on Android)

### Dependencies (Shared)

- `io.ktor:ktor-client-core` + `ktor-client-darwin` (iOS) / `ktor-client-okhttp` (Android)
- `io.ktor:ktor-client-content-negotiation` + `ktor-serialization-kotlinx-json`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `com.squareup.okio:okio` (filesystem)

## iOS App Design

### Target

- iOS 17.0+
- SwiftUI with `@Observable` macro
- MVVM pattern

### Navigation: TabView with 4 Tabs

**Home Tab:**
- Ferry status banner (today's operating hours — regular or holiday)
- Quick info cards (crossing time, capacity, cost: FREE)
- Locations with "Open in Maps" buttons
- External links (Facebook, 511ia.org, Iowa DOT)

**Cameras Tab:**
- Thumbnail grid (3 cameras, snapshot images)
- Tap -> CameraDetailView with inline AVPlayer (HLS)
- Fullscreen toggle via `.fullScreenCover`

**Info Tab:**
- ScrollView with schedule, vehicle restrictions, size limits, contact info

**FAQ Tab:**
- List of expandable/collapsible Q&A items using DisclosureGroup

### Video Player

- SwiftUI wrapper around AVPlayer via UIViewRepresentable
- Snapshot image as thumbnail before playback
- Inline playback with fullscreen option

### View Model

- Single `FerryViewModel` using `@Observable`
- Holds `FerryInfo` state, loading/error states
- Calls shared KMP `FerryRepository` on init and pull-to-refresh

## Data Hosting

- `data/ferry-info.json` in the repo
- Published to GitHub Pages
- App fetches from configurable URL: `https://<username>.github.io/lansing-car-ferry/ferry-info.json`

## Camera Stream URLs

| Camera | HLS Stream |
|--------|-----------|
| Wisconsin Ferry Landing | `https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv09lb/playlist.m3u8` |
| Iowa Ferry Landing | `https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv10lb/playlist.m3u8` |
| Iowa Ferry Queue | `https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv11lb/playlist.m3u8` |

## Deferred (Android)

- `androidApp/` module scaffolded but not built
- Shared module includes `androidMain` source set
- Android development starts once SDK is installed

## Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Cross-platform strategy | KMP shared logic + native UI | Shares business logic, native UX on each platform |
| Shared networking | Ktor | Standard KMP HTTP client |
| Data source | Remote JSON on GitHub Pages | Easy updates without app releases |
| Caching | Persistent + in-memory, stale-while-revalidate | Spotty cell signal in ferry area |
| iOS minimum | 17.0 | Enables @Observable macro, modern SwiftUI |
| Video playback | Inline AVPlayer with fullscreen | Better UX, keeps users in-app |
| Notifications | None | Keeps architecture simple |

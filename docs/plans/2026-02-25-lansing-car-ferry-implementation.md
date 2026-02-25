# Lansing Car Ferry App Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a KMP mobile app (iOS first, Android later) that shows Lansing Car Ferry info, schedule, FAQs, and live HLS camera streams with offline-first caching.

**Architecture:** KMP shared module (Kotlin) handles data models, networking (Ktor), JSON parsing (kotlinx.serialization), and persistent caching (Okio). Native SwiftUI app consumes the shared module via SKIE-enhanced Swift API. MVVM pattern with @Observable view models.

**Tech Stack:** Kotlin 2.1.20, Ktor 3.4.0, kotlinx.serialization, Okio 3.16.2, SKIE 0.10.10, SwiftUI (iOS 17+), AVPlayer (HLS)

---

### Task 1: Initialize KMP Gradle Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `shared/build.gradle.kts`
- Create: `androidApp/build.gradle.kts`

**Step 1: Create `gradle/libs.versions.toml` (version catalog)**

```toml
[versions]
kotlin = "2.1.20"
ktor = "3.4.0"
kotlinx-serialization = "1.7.3"
okio = "3.16.2"
skie = "0.10.10"
agp = "8.8.0"
android-compileSdk = "35"
android-minSdk = "26"
android-targetSdk = "35"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
okio = { module = "com.squareup.okio:okio", version.ref = "okio" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
skie = { id = "co.touchlab.skie", version.ref = "skie" }
```

**Step 2: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LansingCarFerry"
include(":shared")
include(":androidApp")
```

**Step 3: Create `build.gradle.kts` (root)**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.skie) apply false
}
```

**Step 4: Create `gradle.properties`**

```properties
kotlin.code.style=official
kotlin.mpp.androidSourceSetLayoutVersion=2
org.gradle.jvmargs=-Xmx2048M -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
```

**Step 5: Create `shared/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.skie)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    val xcfName = "Shared"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.lansingferry.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

**Step 6: Create `androidApp/build.gradle.kts` (minimal placeholder)**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared"))
        }
    }
}

android {
    namespace = "com.lansingferry.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lansingferry.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
}
```

**Step 7: Create shared module source directories**

```bash
mkdir -p shared/src/commonMain/kotlin/com/lansingferry/shared/model
mkdir -p shared/src/commonMain/kotlin/com/lansingferry/shared/repository
mkdir -p shared/src/commonMain/kotlin/com/lansingferry/shared/network
mkdir -p shared/src/commonMain/kotlin/com/lansingferry/shared/cache
mkdir -p shared/src/iosMain/kotlin/com/lansingferry/shared
mkdir -p shared/src/androidMain/kotlin/com/lansingferry/shared
mkdir -p shared/src/androidMain/AndroidManifest.xml
mkdir -p androidApp/src/androidMain/kotlin/com/lansingferry/android
```

**Step 8: Create `shared/src/androidMain/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
</manifest>
```

**Step 9: Install Gradle wrapper**

Run: `gradle wrapper --gradle-version 8.12`
Expected: `gradle/wrapper/` directory created with `gradlew` and `gradlew.bat`

**Step 10: Verify project compiles (shared iOS framework)**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

**Step 11: Commit**

```bash
git add -A
git commit -m "feat: initialize KMP Gradle project structure"
```

---

### Task 2: Implement Shared Data Models

**Files:**
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/model/FerryInfo.kt`
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/model/Schedule.kt`
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/model/Location.kt`
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/model/Camera.kt`
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/model/VehicleRestrictions.kt`
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/model/FAQ.kt`

**Step 1: Create `FerryInfo.kt` (root model)**

```kotlin
package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class FerryInfo(
    val schedule: Schedule,
    val locations: Locations,
    val cameras: List<Camera>,
    val vehicleRestrictions: VehicleRestrictions,
    val faqs: List<FAQ>,
    val contact: Contact,
    val links: Links,
)

@Serializable
data class Locations(
    val iowa: Location,
    val wisconsin: Location,
)

@Serializable
data class Contact(
    val name: String,
    val email: String,
)

@Serializable
data class Links(
    val facebook: String,
    val traffic: String,
    val iowadot: String,
)
```

**Step 2: Create `Schedule.kt`**

```kotlin
package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val regularHours: RegularHours,
    val holidayHours: HoursWindow,
    val commuterPriorityWindows: List<HoursWindow>,
    val holidays: List<String>,
    val crossingDurationMinutes: Int,
    val approximateCapacity: Int,
    val serviceNote: String,
)

@Serializable
data class RegularHours(
    val wisconsinDeparture: HoursWindow,
    val iowaDeparture: HoursWindow,
)

@Serializable
data class HoursWindow(
    val start: String,
    val end: String,
)
```

**Step 3: Create `Location.kt`**

```kotlin
package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)
```

**Step 4: Create `Camera.kt`**

```kotlin
package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Camera(
    val id: String,
    val name: String,
    val streamUrl: String,
    val snapshotUrl: String,
)
```

**Step 5: Create `VehicleRestrictions.kt`**

```kotlin
package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleRestrictions(
    val allowed: List<String>,
    val prohibited: List<String>,
    val sizeLimits: SizeLimits,
)

@Serializable
data class SizeLimits(
    val heightFeet: Int,
    val lengthFeet: Int,
    val widthFeetInches: String,
    val weightTons: Int,
)
```

**Step 6: Create `FAQ.kt`**

```kotlin
package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class FAQ(
    val question: String,
    val answer: String,
)
```

**Step 7: Verify compilation**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

**Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/com/lansingferry/shared/model/
git commit -m "feat: add shared data models for ferry info"
```

---

### Task 3: Implement Networking Layer

**Files:**
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/network/FerryApiClient.kt`
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/network/HttpClientFactory.kt`

**Step 1: Create `HttpClientFactory.kt`**

```kotlin
package com.lansingferry.shared.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
}
```

**Step 2: Create `FerryApiClient.kt`**

```kotlin
package com.lansingferry.shared.network

import com.lansingferry.shared.model.FerryInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class FerryApiClient(
    private val httpClient: HttpClient = createHttpClient(),
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchFerryInfo(): FerryInfo {
        return httpClient.get("$baseUrl/ferry-info.json").body()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://ajthom90.github.io/lansing-car-ferry"
    }
}
```

**Step 3: Verify compilation**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/lansingferry/shared/network/
git commit -m "feat: add Ktor networking layer for ferry data"
```

---

### Task 4: Implement Persistent Cache

**Files:**
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/cache/CacheStorage.kt`
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/cache/PlatformFileSystem.kt`
- Create: `shared/src/iosMain/kotlin/com/lansingferry/shared/PlatformFileSystem.ios.kt`
- Create: `shared/src/androidMain/kotlin/com/lansingferry/shared/PlatformFileSystem.android.kt`

**Step 1: Create `PlatformFileSystem.kt` (expect declaration)**

```kotlin
package com.lansingferry.shared.cache

import okio.Path

expect fun platformCachePath(): Path
```

**Step 2: Create iOS actual `PlatformFileSystem.ios.kt`**

```kotlin
package com.lansingferry.shared.cache

import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun platformCachePath(): Path {
    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )!!.path!!
    return "$documentDir/ferry-cache.json".toPath()
}
```

**Step 3: Create Android actual `PlatformFileSystem.android.kt`**

```kotlin
package com.lansingferry.shared.cache

import okio.Path
import okio.Path.Companion.toPath

actual fun platformCachePath(): Path {
    return "/data/data/com.lansingferry.android/files/ferry-cache.json".toPath()
}
```

**Step 4: Create `CacheStorage.kt`**

```kotlin
package com.lansingferry.shared.cache

import com.lansingferry.shared.model.FerryInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.buffer

class CacheStorage(
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val cachePath: Path = platformCachePath(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun save(ferryInfo: FerryInfo) {
        val data = json.encodeToString(ferryInfo)
        fileSystem.sink(cachePath).buffer().use { sink ->
            sink.writeUtf8(data)
        }
    }

    fun load(): FerryInfo? {
        if (!fileSystem.exists(cachePath)) return null
        return try {
            val data = fileSystem.source(cachePath).buffer().use { source ->
                source.readUtf8()
            }
            json.decodeFromString<FerryInfo>(data)
        } catch (e: Exception) {
            null
        }
    }
}
```

**Step 5: Verify compilation**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add shared/src/
git commit -m "feat: add persistent cache with Okio filesystem"
```

---

### Task 5: Implement FerryRepository

**Files:**
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/repository/FerryRepository.kt`

**Step 1: Create `FerryRepository.kt`**

```kotlin
package com.lansingferry.shared.repository

import com.lansingferry.shared.cache.CacheStorage
import com.lansingferry.shared.model.FerryInfo
import com.lansingferry.shared.network.FerryApiClient
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FerryRepository(
    private val apiClient: FerryApiClient = FerryApiClient(),
    private val cacheStorage: CacheStorage = CacheStorage(),
) {
    private var inMemoryCache: FerryInfo? = null
    private var lastFetchTime: Instant? = null

    suspend fun getFerryInfo(): FerryResult {
        // 1. Return in-memory cache immediately if fresh
        inMemoryCache?.let { cached ->
            if (!isCacheStale()) {
                return FerryResult.Success(cached)
            }
        }

        // 2. Load from persistent storage if no in-memory cache
        if (inMemoryCache == null) {
            cacheStorage.load()?.let { persisted ->
                inMemoryCache = persisted
            }
        }

        // 3. Try fetching fresh data from network
        return try {
            val fresh = apiClient.fetchFerryInfo()
            inMemoryCache = fresh
            lastFetchTime = Clock.System.now()
            cacheStorage.save(fresh)
            FerryResult.Success(fresh)
        } catch (e: Exception) {
            // 4. Fall back to cached data on network failure
            inMemoryCache?.let { cached ->
                FerryResult.Success(cached)
            } ?: FerryResult.Error("No cached data available. Please check your connection.")
        }
    }

    suspend fun refresh(): FerryResult {
        return try {
            val fresh = apiClient.fetchFerryInfo()
            inMemoryCache = fresh
            lastFetchTime = Clock.System.now()
            cacheStorage.save(fresh)
            FerryResult.Success(fresh)
        } catch (e: Exception) {
            inMemoryCache?.let { cached ->
                FerryResult.Success(cached)
            } ?: FerryResult.Error("Unable to refresh. Please check your connection.")
        }
    }

    private fun isCacheStale(): Boolean {
        val fetchTime = lastFetchTime ?: return true
        val elapsed = Clock.System.now() - fetchTime
        return elapsed.inWholeMinutes >= CACHE_TTL_MINUTES
    }

    companion object {
        private const val CACHE_TTL_MINUTES = 30
    }
}

sealed class FerryResult {
    data class Success(val data: FerryInfo) : FerryResult()
    data class Error(val message: String) : FerryResult()
}
```

**Step 2: Add kotlinx-datetime dependency to `gradle/libs.versions.toml`**

Add to `[versions]`:
```toml
kotlinx-datetime = "0.6.2"
```

Add to `[libraries]`:
```toml
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

Add to `shared/build.gradle.kts` commonMain.dependencies:
```kotlin
implementation(libs.kotlinx.datetime)
```

**Step 3: Verify compilation**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add shared/ gradle/libs.versions.toml
git commit -m "feat: add FerryRepository with offline-first caching"
```

---

### Task 6: Build Shared iOS Framework

**Step 1: Build the XCFramework for iOS Simulator**

Run: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL, framework at `shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework`

**Step 2: Commit (no new files, just verify)**

No commit needed — this verifies the framework builds.

---

### Task 7: Create iOS Xcode Project

**Files:**
- Create: `iosApp/LansingFerry.xcodeproj` (via Xcode or xcodegen)
- Create: `iosApp/LansingFerry/LansingFerryApp.swift`
- Create: `iosApp/LansingFerry/ContentView.swift`
- Create: `iosApp/LansingFerry/Info.plist`

**Step 1: Create Xcode project structure manually**

Create directory structure:
```bash
mkdir -p iosApp/LansingFerry
```

**Step 2: Create `LansingFerryApp.swift`**

```swift
import SwiftUI

@main
struct LansingFerryApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

**Step 3: Create `ContentView.swift` (placeholder tab view)**

```swift
import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            Text("Home")
                .tabItem {
                    Label("Home", systemImage: "house")
                }

            Text("Cameras")
                .tabItem {
                    Label("Cameras", systemImage: "video")
                }

            Text("Info")
                .tabItem {
                    Label("Info", systemImage: "info.circle")
                }

            Text("FAQ")
                .tabItem {
                    Label("FAQ", systemImage: "questionmark.circle")
                }
        }
    }
}
```

**Step 4: Create the Xcode project**

This step requires Xcode. Use `xcodegen` or manually create the project in Xcode:
- Product name: LansingFerry
- Bundle ID: com.lansingferry.ios
- Deployment target: iOS 17.0
- Language: Swift
- Interface: SwiftUI
- Add the Shared.framework from `shared/build/bin/iosSimulatorArm64/debugFramework/`
- Add a Run Script build phase to build the shared framework before compilation:

```bash
cd "$SRCROOT/.."
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

- Add framework search path: `$(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework`

**Step 5: Verify the app builds and launches in simulator**

Run from Xcode: Product > Run (Cmd+R)
Expected: App launches with 4 tab placeholders

**Step 6: Commit**

```bash
git add iosApp/
git commit -m "feat: create iOS Xcode project with tab navigation"
```

---

### Task 8: Create FerryViewModel

**Files:**
- Create: `iosApp/LansingFerry/ViewModels/FerryViewModel.swift`

**Step 1: Create `FerryViewModel.swift`**

```swift
import Foundation
import Shared

@Observable
final class FerryViewModel {
    var ferryInfo: FerryInfo?
    var isLoading = false
    var errorMessage: String?

    private let repository = FerryRepository()

    func loadData() async {
        isLoading = true
        errorMessage = nil

        let result = try? await repository.getFerryInfo()

        switch result {
        case let success as FerryResultSuccess:
            self.ferryInfo = success.data
            self.errorMessage = nil
        case let error as FerryResultError:
            self.errorMessage = error.message
        default:
            self.errorMessage = "An unexpected error occurred."
        }

        isLoading = false
    }

    func refresh() async {
        let result = try? await repository.refresh()

        switch result {
        case let success as FerryResultSuccess:
            self.ferryInfo = success.data
            self.errorMessage = nil
        case let error as FerryResultError:
            self.errorMessage = error.message
        default:
            break
        }
    }
}
```

Note: The exact Swift API generated by SKIE may differ slightly. The `FerryResult` sealed class will be exposed as separate classes (`FerryResultSuccess`, `FerryResultError`) in Swift. Adjust the switch/cast pattern based on what SKIE generates. Check `Shared.framework`'s generated Swift interface.

**Step 2: Update `ContentView.swift` to use the view model**

```swift
import SwiftUI

struct ContentView: View {
    @State private var viewModel = FerryViewModel()

    var body: some View {
        TabView {
            Text("Home")
                .tabItem {
                    Label("Home", systemImage: "house")
                }

            Text("Cameras")
                .tabItem {
                    Label("Cameras", systemImage: "video")
                }

            Text("Info")
                .tabItem {
                    Label("Info", systemImage: "info.circle")
                }

            Text("FAQ")
                .tabItem {
                    Label("FAQ", systemImage: "questionmark.circle")
                }
        }
        .task {
            await viewModel.loadData()
        }
    }
}
```

**Step 3: Verify builds in Xcode**

Run from Xcode: Product > Build (Cmd+B)
Expected: BUILD SUCCEEDED

**Step 4: Commit**

```bash
git add iosApp/
git commit -m "feat: add FerryViewModel consuming shared KMP repository"
```

---

### Task 9: Create the JSON Data File

**Files:**
- Create: `data/ferry-info.json`

**Step 1: Create `data/ferry-info.json`**

```json
{
  "schedule": {
    "regularHours": {
      "wisconsinDeparture": { "start": "05:30", "end": "21:00" },
      "iowaDeparture": { "start": "05:45", "end": "21:15" }
    },
    "holidayHours": {
      "start": "07:00",
      "end": "19:00"
    },
    "commuterPriorityWindows": [
      { "start": "05:30", "end": "07:30" },
      { "start": "15:30", "end": "17:30" }
    ],
    "holidays": [
      "2026-05-25",
      "2026-07-04",
      "2026-09-07",
      "2026-11-11",
      "2026-11-26",
      "2026-12-25",
      "2027-01-01"
    ],
    "crossingDurationMinutes": 15,
    "approximateCapacity": 12,
    "serviceNote": "Continuous, not on a fixed schedule\u2014first-come, first-served"
  },
  "locations": {
    "iowa": {
      "name": "Lansing Marina",
      "description": "North driveway off Front Street",
      "latitude": 43.3578,
      "longitude": -91.2188
    },
    "wisconsin": {
      "name": "WIS 82 Landing",
      "description": "West of Big Slough Boat Landing",
      "latitude": 43.3575,
      "longitude": -91.2050
    }
  },
  "cameras": [
    {
      "id": "wi-landing",
      "name": "Wisconsin Ferry Landing",
      "streamUrl": "https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv09lb/playlist.m3u8",
      "snapshotUrl": "https://iowadotsnapshot.us-east-1.skyvdn.com/thumbs/d2tv09lb.flv.jpg"
    },
    {
      "id": "ia-landing",
      "name": "Iowa Ferry Landing",
      "streamUrl": "https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv10lb/playlist.m3u8",
      "snapshotUrl": "https://iowadotsnapshot.us-east-1.skyvdn.com/thumbs/d2tv10lb.flv.jpg"
    },
    {
      "id": "ia-queue",
      "name": "Iowa Ferry Queue",
      "streamUrl": "https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv11lb/playlist.m3u8",
      "snapshotUrl": "https://iowadotsnapshot.us-east-1.skyvdn.com/thumbs/d2tv11lb.flv.jpg"
    }
  ],
  "vehicleRestrictions": {
    "allowed": [
      "Cars",
      "Motorcycles",
      "Bicycles",
      "Pedestrians",
      "Two-axle trucks under 10 tons"
    ],
    "prohibited": [
      "Trailers",
      "ATVs",
      "UTVs",
      "RVs",
      "Buses",
      "Farm equipment",
      "Hazardous materials"
    ],
    "sizeLimits": {
      "heightFeet": 11,
      "lengthFeet": 25,
      "widthFeetInches": "8 ft 6 in",
      "weightTons": 10
    }
  },
  "faqs": [
    {
      "question": "How much does the ferry cost?",
      "answer": "The ferry is free of charge."
    },
    {
      "question": "Does the ferry run in winter?",
      "answer": "Yes, as long as the river is navigable. Severe ice conditions may force temporary closures."
    },
    {
      "question": "How long is the crossing?",
      "answer": "Approximately 15 minutes including loading and unloading."
    },
    {
      "question": "How long is the detour if I can't take the ferry?",
      "answer": "The detour route takes approximately 60\u201370 minutes."
    },
    {
      "question": "Is the ferry on a fixed schedule?",
      "answer": "No, the ferry runs continuously on a first-come, first-served basis."
    },
    {
      "question": "Can I take photos on the ferry?",
      "answer": "Yes, photography is allowed, but please don't delay operations."
    },
    {
      "question": "What safety equipment is on board?",
      "answer": "Life preservers are available. Follow crew instructions at all times."
    },
    {
      "question": "Are there commuter priority hours?",
      "answer": "Yes, from 5:30\u20137:30 AM and 3:30\u20135:30 PM, the ferry favors commuters."
    },
    {
      "question": "What are the vehicle size limits?",
      "answer": "Maximum 11 ft height, 25 ft length, 8 ft 6 in width, and 10 tons weight."
    }
  ],
  "contact": {
    "name": "Pete Hjelmstad",
    "email": "pete.hjelmstad@iowadot.us"
  },
  "links": {
    "facebook": "https://www.facebook.com/LansingBridge",
    "traffic": "https://511ia.org",
    "iowadot": "https://iowadot.gov/modes-travel/roads-highways/major-construction-projects/mississippi-river-bridge-lansing/car-ferry"
  }
}
```

**Step 2: Commit**

```bash
git add data/
git commit -m "feat: add ferry-info.json data file for GitHub Pages"
```

---

### Task 10: Implement HomeView

**Files:**
- Create: `iosApp/LansingFerry/Views/HomeView.swift`

**Step 1: Create `HomeView.swift`**

```swift
import SwiftUI
import MapKit
import Shared

struct HomeView: View {
    let ferryInfo: FerryInfo

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    statusBanner
                    quickInfoCards
                    locationsSection
                    linksSection
                }
                .padding()
            }
            .navigationTitle("Lansing Car Ferry")
        }
    }

    private var statusBanner: some View {
        VStack(spacing: 4) {
            Text(ferryInfo.schedule.serviceNote)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(.blue.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var quickInfoCards: some View {
        HStack(spacing: 12) {
            InfoCard(
                icon: "clock",
                title: "Crossing",
                value: "\(ferryInfo.schedule.crossingDurationMinutes) min"
            )
            InfoCard(
                icon: "car.2",
                title: "Capacity",
                value: "~\(ferryInfo.schedule.approximateCapacity) vehicles"
            )
            InfoCard(
                icon: "dollarsign.circle",
                title: "Cost",
                value: "FREE"
            )
        }
    }

    private var locationsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Ferry Locations")
                .font(.headline)

            LocationRow(
                label: "Iowa",
                location: ferryInfo.locations.iowa
            )
            LocationRow(
                label: "Wisconsin",
                location: ferryInfo.locations.wisconsin
            )
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var linksSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Resources")
                .font(.headline)

            Link(destination: URL(string: ferryInfo.links.facebook)!) {
                Label("Facebook Updates", systemImage: "link")
            }
            Link(destination: URL(string: ferryInfo.links.traffic)!) {
                Label("511 Iowa Traffic", systemImage: "car")
            }
            Link(destination: URL(string: ferryInfo.links.iowadot)!) {
                Label("Iowa DOT Info", systemImage: "globe")
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct InfoCard: View {
    let icon: String
    let title: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundStyle(.blue)
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.caption)
                .fontWeight(.semibold)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct LocationRow: View {
    let label: String
    let location: Location

    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(label)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text(location.name)
                    .font(.subheadline)
                Text(location.description_)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Button {
                let coordinate = CLLocationCoordinate2D(
                    latitude: location.latitude,
                    longitude: location.longitude
                )
                let placemark = MKPlacemark(coordinate: coordinate)
                let mapItem = MKMapItem(placemark: placemark)
                mapItem.name = location.name
                mapItem.openInMaps()
            } label: {
                Image(systemName: "map")
                    .font(.title3)
            }
        }
        .padding()
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
```

Note: Kotlin's `description` property may be exposed as `description_` in Swift due to NSObject conflict. Check the generated API.

**Step 2: Update `ContentView.swift` to wire in HomeView**

```swift
import SwiftUI

struct ContentView: View {
    @State private var viewModel = FerryViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading && viewModel.ferryInfo == nil {
                ProgressView("Loading ferry info...")
            } else if let ferryInfo = viewModel.ferryInfo {
                TabView {
                    HomeView(ferryInfo: ferryInfo)
                        .tabItem {
                            Label("Home", systemImage: "house")
                        }

                    Text("Cameras")
                        .tabItem {
                            Label("Cameras", systemImage: "video")
                        }

                    Text("Info")
                        .tabItem {
                            Label("Info", systemImage: "info.circle")
                        }

                    Text("FAQ")
                        .tabItem {
                            Label("FAQ", systemImage: "questionmark.circle")
                        }
                }
            } else if let error = viewModel.errorMessage {
                ContentUnavailableView(
                    "Unable to Load",
                    systemImage: "wifi.slash",
                    description: Text(error)
                )
            }
        }
        .task {
            await viewModel.loadData()
        }
    }
}
```

**Step 3: Build and run in simulator**

Expected: Home tab shows status banner, info cards, locations, and links

**Step 4: Commit**

```bash
git add iosApp/
git commit -m "feat: implement HomeView with status, info cards, locations, links"
```

---

### Task 11: Implement LiveCamerasView

**Files:**
- Create: `iosApp/LansingFerry/Views/LiveCamerasView.swift`
- Create: `iosApp/LansingFerry/Views/CameraDetailView.swift`
- Create: `iosApp/LansingFerry/Player/VideoPlayerView.swift`

**Step 1: Create `VideoPlayerView.swift` (AVPlayer wrapper)**

```swift
import SwiftUI
import AVKit

struct VideoPlayerView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()
        let player = AVPlayer(url: url)
        controller.player = player
        controller.allowsPictureInPicturePlayback = false
        player.play()
        return controller
    }

    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {}
}
```

**Step 2: Create `LiveCamerasView.swift`**

```swift
import SwiftUI
import Shared

struct LiveCamerasView: View {
    let cameras: [Camera]

    var body: some View {
        NavigationStack {
            List(cameras, id: \.id) { camera in
                NavigationLink(destination: CameraDetailView(camera: camera)) {
                    HStack(spacing: 12) {
                        AsyncImage(url: URL(string: camera.snapshotUrl)) { image in
                            image
                                .resizable()
                                .aspectRatio(16/9, contentMode: .fill)
                        } placeholder: {
                            Rectangle()
                                .fill(.quaternary)
                                .aspectRatio(16/9, contentMode: .fill)
                                .overlay {
                                    ProgressView()
                                }
                        }
                        .frame(width: 120, height: 68)
                        .clipShape(RoundedRectangle(cornerRadius: 8))

                        Text(camera.name)
                            .font(.subheadline)
                    }
                }
            }
            .navigationTitle("Live Cameras")
        }
    }
}
```

**Step 3: Create `CameraDetailView.swift`**

```swift
import SwiftUI
import Shared

struct CameraDetailView: View {
    let camera: Camera
    @State private var isFullScreen = false

    var body: some View {
        VStack {
            if let url = URL(string: camera.streamUrl) {
                VideoPlayerView(url: url)
                    .aspectRatio(16/9, contentMode: .fit)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding()
                    .onTapGesture {
                        isFullScreen = true
                    }
            }

            Spacer()
        }
        .navigationTitle(camera.name)
        .fullScreenCover(isPresented: $isFullScreen) {
            if let url = URL(string: camera.streamUrl) {
                VideoPlayerView(url: url)
                    .ignoresSafeArea()
                    .overlay(alignment: .topTrailing) {
                        Button {
                            isFullScreen = false
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.title)
                                .foregroundStyle(.white)
                                .padding()
                        }
                    }
            }
        }
    }
}
```

**Step 4: Wire cameras tab into `ContentView.swift`**

Replace the cameras placeholder:
```swift
LiveCamerasView(cameras: ferryInfo.cameras)
    .tabItem {
        Label("Cameras", systemImage: "video")
    }
```

**Step 5: Build and run**

Expected: Cameras tab shows list of 3 cameras with thumbnails. Tapping opens inline HLS player. Tap for fullscreen.

**Step 6: Commit**

```bash
git add iosApp/
git commit -m "feat: implement live camera views with HLS streaming"
```

---

### Task 12: Implement InfoView

**Files:**
- Create: `iosApp/LansingFerry/Views/InfoView.swift`

**Step 1: Create `InfoView.swift`**

```swift
import SwiftUI
import Shared

struct InfoView: View {
    let ferryInfo: FerryInfo

    var body: some View {
        NavigationStack {
            List {
                scheduleSection
                vehicleRestrictionsSection
                sizeLimitsSection
                contactSection
            }
            .navigationTitle("Ferry Info")
        }
    }

    private var scheduleSection: some View {
        Section("Schedule") {
            LabeledContent("Wisconsin Side") {
                Text("\(ferryInfo.schedule.regularHours.wisconsinDeparture.start) – \(ferryInfo.schedule.regularHours.wisconsinDeparture.end)")
            }
            LabeledContent("Iowa Side") {
                Text("\(ferryInfo.schedule.regularHours.iowaDeparture.start) – \(ferryInfo.schedule.regularHours.iowaDeparture.end)")
            }
            LabeledContent("Holiday Hours") {
                Text("\(ferryInfo.schedule.holidayHours.start) – \(ferryInfo.schedule.holidayHours.end)")
            }

            VStack(alignment: .leading, spacing: 4) {
                Text("Commuter Priority")
                    .font(.subheadline)
                ForEach(ferryInfo.schedule.commuterPriorityWindows, id: \.start) { window in
                    Text("\(window.start) – \(window.end)")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    private var vehicleRestrictionsSection: some View {
        Section("Vehicles") {
            DisclosureGroup("Allowed") {
                ForEach(ferryInfo.vehicleRestrictions.allowed, id: \.self) { vehicle in
                    Label(vehicle, systemImage: "checkmark.circle.fill")
                        .foregroundStyle(.green)
                }
            }
            DisclosureGroup("Prohibited") {
                ForEach(ferryInfo.vehicleRestrictions.prohibited, id: \.self) { vehicle in
                    Label(vehicle, systemImage: "xmark.circle.fill")
                        .foregroundStyle(.red)
                }
            }
        }
    }

    private var sizeLimitsSection: some View {
        Section("Size Limits") {
            LabeledContent("Height", value: "\(ferryInfo.vehicleRestrictions.sizeLimits.heightFeet) ft")
            LabeledContent("Length", value: "\(ferryInfo.vehicleRestrictions.sizeLimits.lengthFeet) ft")
            LabeledContent("Width", value: ferryInfo.vehicleRestrictions.sizeLimits.widthFeetInches)
            LabeledContent("Weight", value: "\(ferryInfo.vehicleRestrictions.sizeLimits.weightTons) tons")
        }
    }

    private var contactSection: some View {
        Section("Contact") {
            LabeledContent("Name", value: ferryInfo.contact.name)
            Link(ferryInfo.contact.email, destination: URL(string: "mailto:\(ferryInfo.contact.email)")!)
        }
    }
}
```

**Step 2: Wire info tab into `ContentView.swift`**

Replace the info placeholder:
```swift
InfoView(ferryInfo: ferryInfo)
    .tabItem {
        Label("Info", systemImage: "info.circle")
    }
```

**Step 3: Build and run**

Expected: Info tab shows schedule, restrictions, size limits, contact

**Step 4: Commit**

```bash
git add iosApp/
git commit -m "feat: implement InfoView with schedule and restrictions"
```

---

### Task 13: Implement FAQView

**Files:**
- Create: `iosApp/LansingFerry/Views/FAQView.swift`

**Step 1: Create `FAQView.swift`**

```swift
import SwiftUI
import Shared

struct FAQView: View {
    let faqs: [FAQ]

    var body: some View {
        NavigationStack {
            List(faqs, id: \.question) { faq in
                DisclosureGroup {
                    Text(faq.answer)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } label: {
                    Text(faq.question)
                        .font(.subheadline)
                }
            }
            .navigationTitle("FAQ")
        }
    }
}
```

**Step 2: Wire FAQ tab into `ContentView.swift`**

Replace the FAQ placeholder:
```swift
FAQView(faqs: ferryInfo.faqs)
    .tabItem {
        Label("FAQ", systemImage: "questionmark.circle")
    }
```

**Step 3: Build and run**

Expected: FAQ tab shows expandable Q&A list

**Step 4: Commit**

```bash
git add iosApp/
git commit -m "feat: implement FAQView with expandable Q&A"
```

---

### Task 14: Add Pull-to-Refresh

**Files:**
- Modify: `iosApp/LansingFerry/Views/HomeView.swift`
- Modify: `iosApp/LansingFerry/ContentView.swift`

**Step 1: Add refreshable to ContentView's TabView**

Wrap the TabView content or individual scroll views with `.refreshable`:

In `ContentView.swift`, pass the viewModel to views that need refresh, and add `.refreshable` modifiers on each tab's scroll view.

Alternatively, add a refresh callback:

```swift
HomeView(ferryInfo: ferryInfo) {
    await viewModel.refresh()
}
```

Update `HomeView` to accept an `onRefresh` closure:
```swift
struct HomeView: View {
    let ferryInfo: FerryInfo
    let onRefresh: () async -> Void

    // ... existing body ...
    // Add .refreshable { await onRefresh() } to ScrollView
}
```

**Step 2: Build and run**

Expected: Pull down on Home tab triggers a data refresh

**Step 3: Commit**

```bash
git add iosApp/
git commit -m "feat: add pull-to-refresh support"
```

---

### Task 15: Final Integration & Polish

**Step 1: Review all views render correctly with real data**

Run app in simulator, check each tab:
- Home: status, cards, locations, links
- Cameras: thumbnails load, streams play
- Info: all sections populated
- FAQ: all items expand/collapse

**Step 2: Test offline behavior**

- Launch app with data loaded
- Enable airplane mode in simulator
- Kill and relaunch app
- Expected: App loads from persistent cache, all static content visible

**Step 3: Add App Transport Security exception if needed**

If HLS streams fail to load, add to `Info.plist`:
```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: final integration and polish"
```

---

### Summary of Tasks

| # | Task | Key Files |
|---|------|-----------|
| 1 | Initialize KMP Gradle project | build.gradle.kts, settings.gradle.kts, shared/build.gradle.kts |
| 2 | Shared data models | shared/.../model/*.kt |
| 3 | Networking layer | shared/.../network/*.kt |
| 4 | Persistent cache | shared/.../cache/*.kt, iosMain/..., androidMain/... |
| 5 | FerryRepository | shared/.../repository/FerryRepository.kt |
| 6 | Build iOS framework | (verify only) |
| 7 | Create iOS Xcode project | iosApp/ |
| 8 | FerryViewModel | iosApp/.../ViewModels/FerryViewModel.swift |
| 9 | JSON data file | data/ferry-info.json |
| 10 | HomeView | iosApp/.../Views/HomeView.swift |
| 11 | LiveCamerasView + player | iosApp/.../Views/LiveCamerasView.swift, CameraDetailView.swift, VideoPlayerView.swift |
| 12 | InfoView | iosApp/.../Views/InfoView.swift |
| 13 | FAQView | iosApp/.../Views/FAQView.swift |
| 14 | Pull-to-refresh | Modify HomeView, ContentView |
| 15 | Final integration & polish | Verify all views, test offline |

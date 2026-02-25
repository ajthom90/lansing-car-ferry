# JSON Internationalization Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make server-provided JSON content (FAQs, location names, vehicle types, etc.) translatable by embedding all languages in a single JSON file and resolving the device locale in the shared KMP layer.

**Architecture:** Two-layer model system — `Raw*` models deserialize the multi-language JSON (`Map<String, String>` for localizable fields), then a `LocaleResolver` maps them to the existing `FerryInfo` models with plain `String` fields. Cache stores raw data; resolution happens on read. Native UI code is unchanged.

**Tech Stack:** Kotlin Multiplatform, kotlinx-serialization, Ktor, Okio, SwiftUI, Jetpack Compose

---

### Task 1: Update ferry-info.json with LocalizedString Objects

**Files:**
- Modify: `data/ferry-info.json`

**Step 1: Convert all localizable string fields to `{"en": "..."}` objects**

Replace every localizable string with an object containing an `"en"` key. Non-localizable fields (times, coordinates, URLs, numbers, dates, contact) stay as-is.

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
    "serviceNote": { "en": "Continuous, not on a fixed schedule—first-come, first-served" }
  },
  "locations": {
    "iowa": {
      "name": { "en": "Lansing Marina" },
      "description": { "en": "North driveway off Front Street" },
      "latitude": 43.3695,
      "longitude": -91.2202
    },
    "wisconsin": {
      "name": { "en": "WIS 82 Landing" },
      "description": { "en": "West of Big Slough Boat Landing" },
      "latitude": 43.3667,
      "longitude": -91.2105
    }
  },
  "cameras": [
    {
      "id": "wi-landing",
      "name": { "en": "Wisconsin Ferry Landing" },
      "streamUrl": "https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv09lb/playlist.m3u8",
      "snapshotUrl": "https://iowadotsnapshot.us-east-1.skyvdn.com/thumbs/d2tv09lb.flv.jpg"
    },
    {
      "id": "ia-landing",
      "name": { "en": "Iowa Ferry Landing" },
      "streamUrl": "https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv10lb/playlist.m3u8",
      "snapshotUrl": "https://iowadotsnapshot.us-east-1.skyvdn.com/thumbs/d2tv10lb.flv.jpg"
    },
    {
      "id": "ia-queue",
      "name": { "en": "Iowa Ferry Queue" },
      "streamUrl": "https://iowadotsfs1.us-east-1.skyvdn.com:443/rtplive/d2tv11lb/playlist.m3u8",
      "snapshotUrl": "https://iowadotsnapshot.us-east-1.skyvdn.com/thumbs/d2tv11lb.flv.jpg"
    }
  ],
  "vehicleRestrictions": {
    "allowed": [
      { "en": "Cars" },
      { "en": "Motorcycles" },
      { "en": "Bicycles" },
      { "en": "Pedestrians" },
      { "en": "Two-axle trucks under 10 tons" }
    ],
    "prohibited": [
      { "en": "Trailers" },
      { "en": "ATVs" },
      { "en": "UTVs" },
      { "en": "RVs" },
      { "en": "Buses" },
      { "en": "Farm equipment" },
      { "en": "Hazardous materials" }
    ],
    "sizeLimits": {
      "heightFeet": 11,
      "lengthFeet": 25,
      "widthFeetInches": { "en": "8 ft 6 in" },
      "weightTons": 10
    }
  },
  "faqs": [
    {
      "question": { "en": "How much does the ferry cost?" },
      "answer": { "en": "The ferry is free of charge." }
    },
    {
      "question": { "en": "Does the ferry run in winter?" },
      "answer": { "en": "Yes, as long as the river is navigable. Severe ice conditions may force temporary closures." }
    },
    {
      "question": { "en": "How long is the crossing?" },
      "answer": { "en": "Approximately 15 minutes including loading and unloading." }
    },
    {
      "question": { "en": "How long is the detour if I can't take the ferry?" },
      "answer": { "en": "The detour route takes approximately 60–70 minutes." }
    },
    {
      "question": { "en": "Is the ferry on a fixed schedule?" },
      "answer": { "en": "No, the ferry runs continuously on a first-come, first-served basis." }
    },
    {
      "question": { "en": "Can I take photos on the ferry?" },
      "answer": { "en": "Yes, photography is allowed, but please don't delay operations." }
    },
    {
      "question": { "en": "What safety equipment is on board?" },
      "answer": { "en": "Life preservers are available. Follow crew instructions at all times." }
    },
    {
      "question": { "en": "Are there commuter priority hours?" },
      "answer": { "en": "Yes, from 5:30–7:30 AM and 3:30–5:30 PM, the ferry favors commuters." }
    },
    {
      "question": { "en": "What are the vehicle size limits?" },
      "answer": { "en": "Maximum 11 ft height, 25 ft length, 8 ft 6 in width, and 10 tons weight." }
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
git add data/ferry-info.json
git commit -m "feat: convert ferry-info.json localizable strings to i18n objects"
```

---

### Task 2: Create Raw Model Classes

**Files:**
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/model/raw/RawFerryInfo.kt`

Create a single file with all `Raw*` model classes that mirror the existing models but use `Map<String, String>` (aliased as `LocalizedString`) for every localizable field.

**Step 1: Create the raw models file**

```kotlin
package com.lansingferry.shared.model.raw

import kotlinx.serialization.Serializable

typealias LocalizedString = Map<String, String>

@Serializable
data class RawFerryInfo(
    val schedule: RawSchedule,
    val locations: RawLocations,
    val cameras: List<RawCamera>,
    val vehicleRestrictions: RawVehicleRestrictions,
    val faqs: List<RawFAQ>,
    val contact: RawContact,
    val links: RawLinks,
)

@Serializable
data class RawSchedule(
    val regularHours: RawRegularHours,
    val holidayHours: HoursWindow,
    val commuterPriorityWindows: List<HoursWindow>,
    val holidays: List<String>,
    val crossingDurationMinutes: Int,
    val approximateCapacity: Int,
    val serviceNote: LocalizedString,
)

@Serializable
data class RawRegularHours(
    val wisconsinDeparture: HoursWindow,
    val iowaDeparture: HoursWindow,
)

@Serializable
data class HoursWindow(
    val start: String,
    val end: String,
)

@Serializable
data class RawLocations(
    val iowa: RawLocation,
    val wisconsin: RawLocation,
)

@Serializable
data class RawLocation(
    val name: LocalizedString,
    val description: LocalizedString,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class RawCamera(
    val id: String,
    val name: LocalizedString,
    val streamUrl: String,
    val snapshotUrl: String,
)

@Serializable
data class RawVehicleRestrictions(
    val allowed: List<LocalizedString>,
    val prohibited: List<LocalizedString>,
    val sizeLimits: RawSizeLimits,
)

@Serializable
data class RawSizeLimits(
    val heightFeet: Int,
    val lengthFeet: Int,
    val widthFeetInches: LocalizedString,
    val weightTons: Int,
)

@Serializable
data class RawFAQ(
    val question: LocalizedString,
    val answer: LocalizedString,
)

@Serializable
data class RawContact(
    val name: String,
    val email: String,
)

@Serializable
data class RawLinks(
    val facebook: String,
    val traffic: String,
    val iowadot: String,
)
```

Note: `HoursWindow` is reused from here (not from the existing model file) since it has no localizable fields and is identical. The existing `HoursWindow` in `Schedule.kt` will be removed in Task 4 when we update the existing models.

**Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/lansingferry/shared/model/raw/RawFerryInfo.kt
git commit -m "feat: add raw model classes for i18n JSON deserialization"
```

---

### Task 3: Create LocaleResolver

**Files:**
- Create: `shared/src/commonMain/kotlin/com/lansingferry/shared/i18n/LocaleResolver.kt`

**Step 1: Create the resolver**

```kotlin
package com.lansingferry.shared.i18n

import com.lansingferry.shared.model.*
import com.lansingferry.shared.model.raw.*

object LocaleResolver {

    fun resolve(translations: LocalizedString, locale: String): String {
        // 1. Exact match
        translations[locale]?.let { return it }
        // 2. Language-only match (e.g. "es" from "es-MX")
        val lang = locale.split("-", "_").first()
        if (lang != locale) {
            translations[lang]?.let { return it }
        }
        // 3. English fallback
        return translations["en"] ?: translations.values.firstOrNull() ?: ""
    }

    fun resolve(raw: RawFerryInfo, locale: String): FerryInfo {
        return FerryInfo(
            schedule = resolveSchedule(raw.schedule, locale),
            locations = Locations(
                iowa = resolveLocation(raw.locations.iowa, locale),
                wisconsin = resolveLocation(raw.locations.wisconsin, locale),
            ),
            cameras = raw.cameras.map { resolveCamera(it, locale) },
            vehicleRestrictions = resolveVehicleRestrictions(raw.vehicleRestrictions, locale),
            faqs = raw.faqs.map { resolveFAQ(it, locale) },
            contact = Contact(name = raw.contact.name, email = raw.contact.email),
            links = Links(
                facebook = raw.links.facebook,
                traffic = raw.links.traffic,
                iowadot = raw.links.iowadot,
            ),
        )
    }

    private fun resolveSchedule(raw: RawSchedule, locale: String): Schedule {
        return Schedule(
            regularHours = RegularHours(
                wisconsinDeparture = com.lansingferry.shared.model.HoursWindow(
                    start = raw.regularHours.wisconsinDeparture.start,
                    end = raw.regularHours.wisconsinDeparture.end,
                ),
                iowaDeparture = com.lansingferry.shared.model.HoursWindow(
                    start = raw.regularHours.iowaDeparture.start,
                    end = raw.regularHours.iowaDeparture.end,
                ),
            ),
            holidayHours = com.lansingferry.shared.model.HoursWindow(
                start = raw.holidayHours.start,
                end = raw.holidayHours.end,
            ),
            commuterPriorityWindows = raw.commuterPriorityWindows.map {
                com.lansingferry.shared.model.HoursWindow(start = it.start, end = it.end)
            },
            holidays = raw.holidays,
            crossingDurationMinutes = raw.crossingDurationMinutes,
            approximateCapacity = raw.approximateCapacity,
            serviceNote = resolve(raw.serviceNote, locale),
        )
    }

    private fun resolveLocation(raw: RawLocation, locale: String): Location {
        return Location(
            name = resolve(raw.name, locale),
            description = resolve(raw.description, locale),
            latitude = raw.latitude,
            longitude = raw.longitude,
        )
    }

    private fun resolveCamera(raw: RawCamera, locale: String): Camera {
        return Camera(
            id = raw.id,
            name = resolve(raw.name, locale),
            streamUrl = raw.streamUrl,
            snapshotUrl = raw.snapshotUrl,
        )
    }

    private fun resolveVehicleRestrictions(raw: RawVehicleRestrictions, locale: String): VehicleRestrictions {
        return VehicleRestrictions(
            allowed = raw.allowed.map { resolve(it, locale) },
            prohibited = raw.prohibited.map { resolve(it, locale) },
            sizeLimits = SizeLimits(
                heightFeet = raw.sizeLimits.heightFeet,
                lengthFeet = raw.sizeLimits.lengthFeet,
                widthFeetInches = resolve(raw.sizeLimits.widthFeetInches, locale),
                weightTons = raw.sizeLimits.weightTons,
            ),
        )
    }

    private fun resolveFAQ(raw: RawFAQ, locale: String): FAQ {
        return FAQ(
            question = resolve(raw.question, locale),
            answer = resolve(raw.answer, locale),
        )
    }
}
```

**Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/lansingferry/shared/i18n/LocaleResolver.kt
git commit -m "feat: add LocaleResolver for i18n string resolution"
```

---

### Task 4: Update FerryApiClient to Deserialize RawFerryInfo

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/lansingferry/shared/network/FerryApiClient.kt`

**Step 1: Change the return type to RawFerryInfo**

```kotlin
package com.lansingferry.shared.network

import com.lansingferry.shared.model.raw.RawFerryInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class FerryApiClient(
    private val httpClient: HttpClient = createHttpClient(),
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchFerryInfo(): RawFerryInfo {
        return httpClient.get("$baseUrl/ferry-info.json").body()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://ajthom90.github.io/lansing-car-ferry"
    }
}
```

**Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/lansingferry/shared/network/FerryApiClient.kt
git commit -m "feat: update FerryApiClient to deserialize RawFerryInfo"
```

---

### Task 5: Update CacheStorage to Store RawFerryInfo

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/lansingferry/shared/cache/CacheStorage.kt`

**Step 1: Change save/load to use RawFerryInfo**

```kotlin
package com.lansingferry.shared.cache

import com.lansingferry.shared.model.raw.RawFerryInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

class CacheStorage(
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val cachePath: Path = platformCachePath(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun save(rawFerryInfo: RawFerryInfo) {
        val data = json.encodeToString(rawFerryInfo)
        fileSystem.sink(cachePath).buffer().use { sink ->
            sink.writeUtf8(data)
        }
    }

    fun load(): RawFerryInfo? {
        if (!fileSystem.exists(cachePath)) return null
        return try {
            val data = fileSystem.source(cachePath).buffer().use { source ->
                source.readUtf8()
            }
            json.decodeFromString<RawFerryInfo>(data)
        } catch (e: Exception) {
            null
        }
    }
}
```

**Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/lansingferry/shared/cache/CacheStorage.kt
git commit -m "feat: update CacheStorage to persist RawFerryInfo"
```

---

### Task 6: Update FerryRepository to Accept Locale and Resolve

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/lansingferry/shared/repository/FerryRepository.kt`

**Step 1: Add locale parameter, store raw data, resolve on return**

```kotlin
package com.lansingferry.shared.repository

import com.lansingferry.shared.cache.CacheStorage
import com.lansingferry.shared.i18n.LocaleResolver
import com.lansingferry.shared.model.FerryInfo
import com.lansingferry.shared.model.raw.RawFerryInfo
import com.lansingferry.shared.network.FerryApiClient
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FerryRepository(
    private val apiClient: FerryApiClient = FerryApiClient(),
    private val cacheStorage: CacheStorage = CacheStorage(),
) {
    private var inMemoryCache: RawFerryInfo? = null
    private var lastFetchTime: Instant? = null

    suspend fun getFerryInfo(locale: String = "en"): FerryResult {
        // 1. Return in-memory cache immediately if fresh
        inMemoryCache?.let { cached ->
            if (!isCacheStale()) {
                return FerryResult.Success(LocaleResolver.resolve(cached, locale))
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
            FerryResult.Success(LocaleResolver.resolve(fresh, locale))
        } catch (e: Exception) {
            // 4. Fall back to cached data on network failure
            inMemoryCache?.let { cached ->
                FerryResult.Success(LocaleResolver.resolve(cached, locale))
            } ?: FerryResult.Error("No cached data available. Please check your connection.")
        }
    }

    suspend fun refresh(locale: String = "en"): FerryResult {
        return try {
            val fresh = apiClient.fetchFerryInfo()
            inMemoryCache = fresh
            lastFetchTime = Clock.System.now()
            cacheStorage.save(fresh)
            FerryResult.Success(LocaleResolver.resolve(fresh, locale))
        } catch (e: Exception) {
            inMemoryCache?.let { cached ->
                FerryResult.Success(LocaleResolver.resolve(cached, locale))
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

        fun create(): FerryRepository = FerryRepository()
    }
}

sealed class FerryResult {
    data class Success(val data: FerryInfo) : FerryResult()
    data class Error(val message: String) : FerryResult()
}
```

Note: The `locale` parameter defaults to `"en"` so existing callers continue to work without changes until we wire up the platform locale in Task 7.

**Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/lansingferry/shared/repository/FerryRepository.kt
git commit -m "feat: update FerryRepository to accept locale and resolve i18n strings"
```

---

### Task 7: Update Android ViewModel to Pass Device Locale

**Files:**
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/viewmodel/FerryViewModel.kt`

**Step 1: Pass `Locale.getDefault().language` to repository calls**

```kotlin
package com.lansingferry.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lansingferry.shared.model.FerryInfo
import com.lansingferry.shared.repository.FerryRepository
import com.lansingferry.shared.repository.FerryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class FerryUiState(
    val ferryInfo: FerryInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class FerryViewModel : ViewModel() {
    private val repository = FerryRepository.create()

    private val _uiState = MutableStateFlow(FerryUiState())
    val uiState: StateFlow<FerryUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val locale = Locale.getDefault().language
            when (val result = repository.getFerryInfo(locale)) {
                is FerryResult.Success -> {
                    _uiState.value = FerryUiState(ferryInfo = result.data)
                }
                is FerryResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val locale = Locale.getDefault().language
            when (val result = repository.refresh(locale)) {
                is FerryResult.Success -> {
                    _uiState.value = FerryUiState(ferryInfo = result.data)
                }
                is FerryResult.Error -> {
                    // Keep existing data on refresh failure
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}
```

**Step 2: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/viewmodel/FerryViewModel.kt
git commit -m "feat(android): pass device locale to FerryRepository"
```

---

### Task 8: Update iOS ViewModel to Pass Device Locale

**Files:**
- Modify: `iosApp/LansingFerry/ViewModels/FerryViewModel.swift`

**Step 1: Pass `Locale.current.language.languageCode` to repository calls**

```swift
import Foundation
import Shared

@Observable
final class FerryViewModel {
    var ferryInfo: FerryInfo?
    var isLoading = false
    var errorMessage: String?

    private let repository = FerryRepository.companion.create()

    private var deviceLocale: String {
        Locale.current.language.languageCode?.identifier ?? "en"
    }

    func loadData() async {
        isLoading = true
        errorMessage = nil

        do {
            let result = try await repository.getFerryInfo(locale: deviceLocale)

            switch onEnum(of: result) {
            case .success(let success):
                self.ferryInfo = success.data
                self.errorMessage = nil
            case .error(let error):
                self.errorMessage = error.message
            }
        } catch {
            self.errorMessage = String(localized: "An unexpected error occurred.")
        }

        isLoading = false
    }

    func refresh() async {
        do {
            let result = try await repository.refresh(locale: deviceLocale)

            switch onEnum(of: result) {
            case .success(let success):
                self.ferryInfo = success.data
                self.errorMessage = nil
            case .error(let error):
                self.errorMessage = error.message
            }
        } catch {
            // Silently ignore refresh errors if we already have data
        }
    }
}
```

**Step 2: Commit**

```bash
git add iosApp/LansingFerry/ViewModels/FerryViewModel.swift
git commit -m "feat(ios): pass device locale to FerryRepository"
```

---

### Task 9: Build Verification

**Step 1: Build Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 2: Build iOS**

Run: `cd iosApp && xcodegen generate && xcodebuild -project LansingFerry.xcodeproj -scheme LansingFerry -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

**Step 3: Fix any build errors**

If there are compilation errors (e.g. ambiguous `HoursWindow` references between `model` and `model.raw` packages), fix them. The raw `HoursWindow` should be in the `model.raw` package, and the existing `HoursWindow` in `model.Schedule` stays as-is. The `LocaleResolver` converts between them explicitly.

**Step 4: Deploy updated JSON to GitHub Pages**

Run: `git push`

The updated `data/ferry-info.json` needs to be live on GitHub Pages for the app to work with the new format.

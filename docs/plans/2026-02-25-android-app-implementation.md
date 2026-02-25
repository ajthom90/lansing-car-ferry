# Android App Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the Jetpack Compose frontend for the Lansing Car Ferry Android app, achieving feature parity with the iOS app.

**Architecture:** Single-activity Compose app. The shared KMP module (`FerryRepository`, data models, networking, caching) is already complete. The Android app only adds UI: Jetpack Compose screens, an AndroidX ViewModel, AndroidX Media3 ExoPlayer for HLS video, and Compose Navigation with a bottom bar. 4 tabs: Home, Cameras, Info, FAQ.

**Tech Stack:** Kotlin 2.3.10, Jetpack Compose (BOM 2026.01.01), Material 3, Compose Navigation 2.9.7, AndroidX Media3 1.9.2, Coil 3.4.0, AndroidX Activity Compose 1.12.4

---

### Task 1: Update Gradle Configuration

Add all Android/Compose dependencies to the version catalog and the `androidApp/build.gradle.kts`.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `androidApp/build.gradle.kts`
- Modify: `build.gradle.kts` (root — add compose-compiler plugin with `apply false`)

**Step 1: Add versions and libraries to version catalog**

Add these entries to `gradle/libs.versions.toml`:

```toml
# Add to [versions]
compose-bom = "2026.01.01"
activity-compose = "1.12.4"
navigation-compose = "2.9.7"
media3 = "1.9.2"
coil = "3.4.0"

# Add to [libraries]
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose" }
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-exoplayer-hls = { module = "androidx.media3:media3-exoplayer-hls", version.ref = "media3" }
media3-ui = { module = "androidx.media3:media3-ui", version.ref = "media3" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor3 = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }

# Add to [plugins]
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

**Step 2: Add compose-compiler plugin to root build.gradle.kts**

Add `alias(libs.plugins.compose.compiler) apply false` to the root `build.gradle.kts` plugins block.

**Step 3: Rewrite androidApp/build.gradle.kts**

Replace `androidApp/build.gradle.kts` with:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared"))
            implementation(platform(libs.compose.bom))
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.activity.compose)
            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.exoplayer.hls)
            implementation(libs.media3.ui)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
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

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}
```

**Step 4: Verify Gradle sync succeeds**

Run: `./gradlew :androidApp:dependencies --configuration androidMainCompileClasspath 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts androidApp/build.gradle.kts
git commit -m "feat(android): add Compose, Media3, Coil, Navigation dependencies"
```

---

### Task 2: Create AndroidManifest, Theme, and MainActivity

Set up the app entry point with a Material 3 theme and an empty Compose surface.

**Files:**
- Create: `androidApp/src/androidMain/AndroidManifest.xml`
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/theme/Theme.kt`
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/MainActivity.kt`

**Step 1: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="Lansing Ferry"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

**Step 2: Create Theme.kt**

```kotlin
package com.lansingferry.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FerryBlue = Color(0xFF1A8FE3)
private val FerryBlueDark = Color(0xFF0B5FA5)

private val LightColorScheme = lightColorScheme(
    primary = FerryBlue,
    onPrimary = Color.White,
    primaryContainer = FerryBlue.copy(alpha = 0.12f),
    onPrimaryContainer = FerryBlueDark,
    secondary = FerryBlueDark,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5F5F5),
)

@Composable
fun LansingFerryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
```

**Step 3: Create MainActivity**

```kotlin
package com.lansingferry.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.lansingferry.android.ui.theme.LansingFerryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LansingFerryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text("Lansing Car Ferry")
                }
            }
        }
    }
}
```

**Step 4: Build the app**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add androidApp/src/androidMain/
git commit -m "feat(android): add manifest, theme, and MainActivity with Compose"
```

---

### Task 3: Create FerryViewModel

AndroidX ViewModel wrapping the shared `FerryRepository`. Exposes UI state as `StateFlow`.

**Files:**
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/viewmodel/FerryViewModel.kt`

**Step 1: Create FerryViewModel**

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
            when (val result = repository.getFerryInfo()) {
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
            when (val result = repository.refresh()) {
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

**Step 2: Verify build**

Run: `./gradlew :androidApp:compileDebugKotlinAndroid 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/viewmodel/
git commit -m "feat(android): add FerryViewModel with StateFlow UI state"
```

---

### Task 4: Create Navigation Shell

Bottom navigation bar with 4 tabs + NavHost routing. Wire into MainActivity.

**Files:**
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/navigation/FerryNavigation.kt`
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/MainActivity.kt`

**Step 1: Create FerryNavigation.kt**

```kotlin
package com.lansingferry.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lansingferry.shared.model.FerryInfo
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object CamerasRoute
@Serializable data object InfoRoute
@Serializable data object FaqRoute

data class TopLevelRoute(
    val label: String,
    val route: Any,
    val icon: ImageVector,
)

val topLevelRoutes = listOf(
    TopLevelRoute("Home", HomeRoute, Icons.Default.Home),
    TopLevelRoute("Cameras", CamerasRoute, Icons.Default.Videocam),
    TopLevelRoute("Info", InfoRoute, Icons.Default.Info),
    TopLevelRoute("FAQ", FaqRoute, Icons.Default.QuestionAnswer),
)

@Composable
fun FerryNavigation(
    ferryInfo: FerryInfo,
    onRefresh: () -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                topLevelRoutes.forEach { route ->
                    NavigationBarItem(
                        icon = { Icon(route.icon, contentDescription = route.label) },
                        label = { Text(route.label) },
                        selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(route.route::class)
                        } == true,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<HomeRoute> {
                // Placeholder — replaced in Task 5
                Box(Modifier.fillMaxSize()) { Text("Home") }
            }
            composable<CamerasRoute> {
                // Placeholder — replaced in Task 8
                Box(Modifier.fillMaxSize()) { Text("Cameras") }
            }
            composable<InfoRoute> {
                // Placeholder — replaced in Task 6
                Box(Modifier.fillMaxSize()) { Text("Info") }
            }
            composable<FaqRoute> {
                // Placeholder — replaced in Task 7
                Box(Modifier.fillMaxSize()) { Text("FAQ") }
            }
        }
    }
}
```

**Step 2: Update MainActivity to use ViewModel + Navigation**

Replace `MainActivity.kt`:

```kotlin
package com.lansingferry.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lansingferry.android.ui.navigation.FerryNavigation
import com.lansingferry.android.ui.theme.LansingFerryTheme
import com.lansingferry.android.viewmodel.FerryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LansingFerryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: FerryViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsState()

                    when {
                        uiState.isLoading && uiState.ferryInfo == null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.ferryInfo != null -> {
                            FerryNavigation(
                                ferryInfo = uiState.ferryInfo!!,
                                onRefresh = { viewModel.refresh() },
                            )
                        }
                        uiState.errorMessage != null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(uiState.errorMessage!!)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

**Step 3: Build the app**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/
git commit -m "feat(android): add navigation shell with 4-tab bottom bar"
```

---

### Task 5: Create HomeScreen

Service note banner, quick info cards, location rows with "Open in Maps", resource links.

**Files:**
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/screens/HomeScreen.kt`
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/navigation/FerryNavigation.kt` (wire in HomeScreen)

**Step 1: Create HomeScreen.kt**

```kotlin
package com.lansingferry.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lansingferry.shared.model.FerryInfo
import com.lansingferry.shared.model.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(ferryInfo: FerryInfo) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Lansing Car Ferry") })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = ferryInfo.schedule.serviceNote,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                )
            }

            // Quick info cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                InfoCard(
                    icon = Icons.Default.AccessTime,
                    title = "Crossing",
                    value = "${ferryInfo.schedule.crossingDurationMinutes} min",
                    modifier = Modifier.weight(1f),
                )
                InfoCard(
                    icon = Icons.Default.DirectionsCar,
                    title = "Capacity",
                    value = "~${ferryInfo.schedule.approximateCapacity} vehicles",
                    modifier = Modifier.weight(1f),
                )
                InfoCard(
                    icon = Icons.Default.MoneyOff,
                    title = "Cost",
                    value = "FREE",
                    modifier = Modifier.weight(1f),
                )
            }

            // Locations
            Text("Ferry Locations", style = MaterialTheme.typography.titleMedium)

            LocationRow(label = "Iowa", location = ferryInfo.locations.iowa) {
                val uri = Uri.parse("geo:${ferryInfo.locations.iowa.latitude},${ferryInfo.locations.iowa.longitude}?q=${ferryInfo.locations.iowa.latitude},${ferryInfo.locations.iowa.longitude}(${Uri.encode(ferryInfo.locations.iowa.name)})")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            LocationRow(label = "Wisconsin", location = ferryInfo.locations.wisconsin) {
                val uri = Uri.parse("geo:${ferryInfo.locations.wisconsin.latitude},${ferryInfo.locations.wisconsin.longitude}?q=${ferryInfo.locations.wisconsin.latitude},${ferryInfo.locations.wisconsin.longitude}(${Uri.encode(ferryInfo.locations.wisconsin.name)})")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            // Links
            Text("Resources", style = MaterialTheme.typography.titleMedium)

            LinkRow(icon = Icons.Default.Link, label = "Facebook Updates") {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.facebook)))
            }
            LinkRow(icon = Icons.Default.Traffic, label = "511 Iowa Traffic") {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.traffic)))
            }
            LinkRow(icon = Icons.Default.Language, label = "Iowa DOT Info") {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ferryInfo.links.iowadot)))
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun LocationRow(
    label: String,
    location: Location,
    onMapClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(location.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    location.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onMapClick) {
                Icon(Icons.Default.Map, contentDescription = "Open in Maps", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
        }
    }
}
```

**Step 2: Wire HomeScreen into FerryNavigation.kt**

Replace the `composable<HomeRoute>` placeholder:

```kotlin
composable<HomeRoute> {
    HomeScreen(ferryInfo = ferryInfo)
}
```

Add import: `import com.lansingferry.android.ui.screens.HomeScreen`

**Step 3: Build**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/
git commit -m "feat(android): add HomeScreen with info cards, locations, and links"
```

---

### Task 6: Create InfoScreen

Schedule with AM/PM times, vehicle restrictions, size limits.

**Files:**
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/screens/InfoScreen.kt`
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/navigation/FerryNavigation.kt`

**Step 1: Create InfoScreen.kt**

```kotlin
package com.lansingferry.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lansingferry.shared.model.FerryInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(ferryInfo: FerryInfo) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Ferry Info") })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Schedule section
            SectionHeader("Schedule")
            InfoRow("Wisconsin Side", formatRange(
                ferryInfo.schedule.regularHours.wisconsinDeparture.start,
                ferryInfo.schedule.regularHours.wisconsinDeparture.end,
            ))
            InfoRow("Iowa Side", formatRange(
                ferryInfo.schedule.regularHours.iowaDeparture.start,
                ferryInfo.schedule.regularHours.iowaDeparture.end,
            ))
            InfoRow("Holiday Hours", formatRange(
                ferryInfo.schedule.holidayHours.start,
                ferryInfo.schedule.holidayHours.end,
            ))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Commuter Priority", style = MaterialTheme.typography.titleSmall)
                    ferryInfo.schedule.commuterPriorityWindows.forEach { window ->
                        Text(
                            formatRange(window.start, window.end),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Vehicle restrictions
            SectionHeader("Vehicles")

            ExpandableList("Allowed", ferryInfo.vehicleRestrictions.allowed) { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                    Text(item, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            ExpandableList("Prohibited", ferryInfo.vehicleRestrictions.prohibited) { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFF44336))
                    Text(item, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Size limits
            SectionHeader("Size Limits")
            InfoRow("Height", "${ferryInfo.vehicleRestrictions.sizeLimits.heightFeet} ft")
            InfoRow("Length", "${ferryInfo.vehicleRestrictions.sizeLimits.lengthFeet} ft")
            InfoRow("Width", ferryInfo.vehicleRestrictions.sizeLimits.widthFeetInches)
            InfoRow("Weight", "${ferryInfo.vehicleRestrictions.sizeLimits.weightTons} tons")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun InfoRow(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun <T> ExpandableList(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items.forEach { item -> itemContent(item) }
                }
            }
        }
    }
}

private fun formatTime(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time
    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1].toIntOrNull() ?: return time
    val period = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return if (minute == 0) "$displayHour $period" else "$displayHour:${"%02d".format(minute)} $period"
}

private fun formatRange(start: String, end: String): String {
    return "${formatTime(start)} – ${formatTime(end)}"
}
```

**Step 2: Wire InfoScreen into FerryNavigation.kt**

Replace the `composable<InfoRoute>` placeholder:

```kotlin
composable<InfoRoute> {
    InfoScreen(ferryInfo = ferryInfo)
}
```

Add import: `import com.lansingferry.android.ui.screens.InfoScreen`

**Step 3: Build**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/
git commit -m "feat(android): add InfoScreen with schedule, vehicles, and size limits"
```

---

### Task 7: Create FAQScreen

Expandable question/answer list.

**Files:**
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/screens/FAQScreen.kt`
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/navigation/FerryNavigation.kt`

**Step 1: Create FAQScreen.kt**

```kotlin
package com.lansingferry.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lansingferry.shared.model.FAQ

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(faqs: List<FAQ>) {
    val expandedItems = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("FAQ") })

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(faqs, key = { it.question }) { faq ->
                val isExpanded = expandedItems[faq.question] == true

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().clickable {
                        expandedItems[faq.question] = !isExpanded
                    },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                faq.question,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                            )
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                            )
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Text(
                                faq.answer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
```

**Step 2: Wire FAQScreen into FerryNavigation.kt**

Replace the `composable<FaqRoute>` placeholder:

```kotlin
composable<FaqRoute> {
    FAQScreen(faqs = ferryInfo.faqs)
}
```

Add import: `import com.lansingferry.android.ui.screens.FAQScreen`

**Step 3: Build**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/
git commit -m "feat(android): add FAQScreen with expandable Q&A cards"
```

---

### Task 8: Create LiveCamerasScreen and CameraDetailScreen

Camera list with Coil thumbnails, fullscreen ExoPlayer for HLS streams.

**Files:**
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/screens/LiveCamerasScreen.kt`
- Create: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/screens/CameraDetailScreen.kt`
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/navigation/FerryNavigation.kt`

**Step 1: Create LiveCamerasScreen.kt**

```kotlin
package com.lansingferry.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lansingferry.shared.model.Camera

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCamerasScreen(
    cameras: List<Camera>,
    onCameraClick: (Camera) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Live Cameras") })

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            items(cameras, key = { it.id }) { camera ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onCameraClick(camera) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        AsyncImage(
                            model = camera.snapshotUrl,
                            contentDescription = camera.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Text(
                            camera.name,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        }
    }
}
```

**Step 2: Create CameraDetailScreen.kt**

```kotlin
package com.lansingferry.android.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailScreen(
    cameraName: String,
    streamUrl: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(streamUrl)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(cameraName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .padding(16.dp),
        )
    }
}
```

**Step 3: Update FerryNavigation.kt — add camera detail route and wire screens**

Add a new serializable route for camera detail:

```kotlin
@Serializable
data class CameraDetailRoute(val cameraName: String, val streamUrl: String)
```

Replace the `composable<CamerasRoute>` placeholder and add the detail route:

```kotlin
composable<CamerasRoute> {
    LiveCamerasScreen(
        cameras = ferryInfo.cameras,
        onCameraClick = { camera ->
            navController.navigate(CameraDetailRoute(camera.name, camera.streamUrl))
        },
    )
}
composable<CameraDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<CameraDetailRoute>()
    CameraDetailScreen(
        cameraName = route.cameraName,
        streamUrl = route.streamUrl,
        onBack = { navController.popBackStack() },
    )
}
```

Add imports:
```kotlin
import com.lansingferry.android.ui.screens.LiveCamerasScreen
import com.lansingferry.android.ui.screens.CameraDetailScreen
import androidx.navigation.toRoute
```

**Step 4: Build**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/
git commit -m "feat(android): add LiveCamerasScreen and CameraDetailScreen with ExoPlayer"
```

---

### Task 9: Add Pull-to-Refresh on HomeScreen

**Files:**
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/screens/HomeScreen.kt`
- Modify: `androidApp/src/androidMain/kotlin/com/lansingferry/android/ui/navigation/FerryNavigation.kt`

**Step 1: Add pull-to-refresh to HomeScreen**

Wrap the scrollable content in `HomeScreen` with `PullToRefreshBox`. Update the function signature to accept `isRefreshing` and `onRefresh`:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ferryInfo: FerryInfo,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
```

After the `TopAppBar`, replace the scrollable `Column` with:

```kotlin
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = Modifier.fillMaxSize(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ... existing content unchanged ...
    }
}
```

Add import: `import androidx.compose.material3.ExperimentalMaterial3Api` (already present) and `import androidx.compose.material3.pulltorefresh.PullToRefreshBox`

**Step 2: Update navigation to pass refresh params**

In FerryNavigation.kt, update the HomeRoute composable:

```kotlin
composable<HomeRoute> {
    HomeScreen(
        ferryInfo = ferryInfo,
        isRefreshing = false,
        onRefresh = onRefresh,
    )
}
```

**Step 3: Build**

Run: `./gradlew :androidApp:assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add androidApp/src/androidMain/kotlin/com/lansingferry/android/
git commit -m "feat(android): add pull-to-refresh on HomeScreen"
```

---

### Task 10: Final Build Verification

Verify the complete app builds and the APK is produced.

**Step 1: Full clean build**

Run: `./gradlew clean :androidApp:assembleDebug 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

**Step 2: Verify APK exists**

Run: `ls -lh androidApp/build/outputs/apk/debug/`
Expected: `androidApp-debug.apk` file listed

**Step 3: Commit any remaining changes**

If there are any remaining changes from build fixes, commit them.

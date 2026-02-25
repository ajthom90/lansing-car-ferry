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
import com.lansingferry.android.ui.screens.HomeScreen
import com.lansingferry.android.ui.screens.InfoScreen
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
                HomeScreen(ferryInfo = ferryInfo)
            }
            composable<CamerasRoute> {
                Box(Modifier.fillMaxSize()) { Text("Cameras") }
            }
            composable<InfoRoute> {
                InfoScreen(ferryInfo = ferryInfo)
            }
            composable<FaqRoute> {
                Box(Modifier.fillMaxSize()) { Text("FAQ") }
            }
        }
    }
}

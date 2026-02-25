package dev.ajthom.lansingferry.ui.navigation

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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.ajthom.lansingferry.ui.screens.CameraDetailScreen
import dev.ajthom.lansingferry.ui.screens.HomeScreen
import dev.ajthom.lansingferry.ui.screens.FAQScreen
import dev.ajthom.lansingferry.ui.screens.InfoScreen
import dev.ajthom.lansingferry.ui.screens.LiveCamerasScreen
import dev.ajthom.lansingferry.R
import dev.ajthom.lansingferry.shared.model.FerryInfo
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object CamerasRoute
@Serializable data class CameraDetailRoute(val cameraName: String, val streamUrl: String)
@Serializable data object InfoRoute
@Serializable data object FaqRoute

data class TopLevelRoute(
    val labelRes: Int,
    val route: Any,
    val icon: ImageVector,
)

val topLevelRoutes = listOf(
    TopLevelRoute(R.string.tab_home, HomeRoute, Icons.Default.Home),
    TopLevelRoute(R.string.tab_cameras, CamerasRoute, Icons.Default.Videocam),
    TopLevelRoute(R.string.tab_info, InfoRoute, Icons.Default.Info),
    TopLevelRoute(R.string.tab_faq, FaqRoute, Icons.Default.QuestionAnswer),
)

@Composable
fun FerryNavigation(
    ferryInfo: FerryInfo,
    isRefreshing: Boolean,
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
                        icon = { Icon(route.icon, contentDescription = stringResource(route.labelRes)) },
                        label = { Text(stringResource(route.labelRes)) },
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
                HomeScreen(
                    ferryInfo = ferryInfo,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                )
            }
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
            composable<InfoRoute> {
                InfoScreen(ferryInfo = ferryInfo)
            }
            composable<FaqRoute> {
                FAQScreen(faqs = ferryInfo.faqs)
            }
        }
    }
}

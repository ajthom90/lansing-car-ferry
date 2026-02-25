package dev.ajthom.lansingferry.ui.theme

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

@file:OptIn(ExperimentalTvMaterial3Api::class)

package xyruscode.tv.launcher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5AC8FA),
    onPrimary = Color(0xFF00131F),
    background = Color(0xFF0B0E14),
    onBackground = Color(0xFFECEFF4),
    surface = Color(0xFF141922),
    onSurface = Color(0xFFECEFF4),
    surfaceVariant = Color(0xFF1E2530),
)

@Composable
fun TvLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}

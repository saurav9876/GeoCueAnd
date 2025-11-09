package com.geocue.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val ColorWhite = androidx.compose.ui.graphics.Color.White
private val ColorBlack = androidx.compose.ui.graphics.Color(0xFF1C1B1F)

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = ColorWhite,
    primaryContainer = BlueSecondary,
    onPrimaryContainer = ColorWhite,
    secondary = BlueAccent,
    onSecondary = ColorWhite,
    background = SurfaceLight,
    onBackground = ColorBlack,
    surface = ColorWhite,
    onSurface = ColorBlack,
)

private val DarkColors = darkColorScheme(
    primary = BlueAccent,
    onPrimary = ColorBlack,
    primaryContainer = BluePrimary,
    onPrimaryContainer = ColorWhite,
    secondary = BlueAccent,
    onSecondary = ColorBlack,
    background = SurfaceDark,
    onBackground = ColorWhite,
    surface = SurfaceDark,
    onSurface = ColorWhite,
)

@Composable
fun GeoCueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val sysUiController = rememberSystemUiController()

    SideEffect {
        sysUiController.setStatusBarColor(colorScheme.background, darkIcons = !darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

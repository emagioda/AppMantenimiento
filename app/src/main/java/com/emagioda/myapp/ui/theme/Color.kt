package com.emagioda.myapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val BlueDark = Color(0xFF1D2E44)
val GrayMid = Color(0xFFCFD5DC)
val White = Color(0xFFFFFFFF)

val LightColors = lightColorScheme(
    primary = BlueDark,
    onPrimary = White,
    background = White,
    surface = White,
    onSurface = Color(0xFF223042),
    outline = GrayMid
)

val DarkColors = darkColorScheme(
    primary = BlueDark,
    onPrimary = White,
    background = Color(0xFF0E1116),
    surface = Color(0xFF12161C),
    onSurface = Color(0xFFE7ECF2),
    outline = Color(0xFF3C4754)
)

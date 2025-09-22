package com.emagioda.myapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Paleta base
val BlueDark = Color(0xFF1D2E44)
val BlueDarkVariant = Color(0xFF0F2135)
val GrayLight = Color(0xFFF3F5F7)
val GrayMid = Color(0xFFCFD5DC)
val TextPrimary = Color(0xFF223042)
val TextSecondary = Color(0xFF707C89)
val White = Color(0xFFFFFFFF)

// Esquemas Material3
val LightColors = lightColorScheme(
    primary = BlueDark,
    onPrimary = White,
    background = White,
    surface = White,
    onSurface = TextPrimary,
    outline = GrayMid
)

val DarkColors = darkColorScheme(
    primary = BlueDark,            // mantenemos el azul como acento
    onPrimary = White,
    background = Color(0xFF0E1116),
    surface = Color(0xFF12161C),
    onSurface = Color(0xFFE7ECF2),
    outline = Color(0xFF3C4754)
)

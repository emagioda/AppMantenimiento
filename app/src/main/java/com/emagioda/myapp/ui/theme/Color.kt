package com.emagioda.myapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val BlueDark = Color(0xFF1D2E44)
val WarningYellow = Color(0xFFFFD600)
val White = Color(0xFFFFFFFF)
val PremiumBackgroundTop = Color(0xFF091019)
val PremiumBackgroundBottom = Color(0xFF111A26)
val PremiumSurfaceTop = Color(0xFF182333)
val PremiumSurfaceRaised = Color(0xFF1F2B3D)
val PremiumSurfaceSoft = Color(0xFF243247)
val PremiumStroke = Color(0xFF33465F)
val PremiumStrokeStrong = Color(0xFF4D6988)
val PremiumBlue = Color(0xFF8AB6FF)
val PremiumCyan = Color(0xFF74D7F7)
val PremiumOrange = Color(0xFFFFB067)
val PremiumPurple = Color(0xFF96A9FF)
val PremiumSuccess = Color(0xFF53C983)
val PremiumDanger = Color(0xFFFF7B7B)
val PremiumScannerLaser = Color(0xFFFF8B38)
val PremiumScannerCorner = Color(0xFF78E3FF)
val ResultResolvedGreen = Color(0xFF2E7D32)
val ResultWarningAmber = Color(0xFFF9A825)
val ResultFaultRed = Color(0xFFC62828)
val SupportButtonContainer = Color(0xFFD8E3F0)
val SupportButtonContent = Color(0xFF1D2E44)
val HistoryPendingAmber = Color(0xFFF0B54D)
val HistoryInProgressBlue = Color(0xFF6EA8C6)
val HistoryFinalizedGreen = Color(0xFF5BB37D)
val HistoryTimelineLine = Color(0xFF3A4653)

val DarkColors = darkColorScheme(
    primary = BlueDark,
    onPrimary = White,
    primaryContainer = PremiumSurfaceRaised,
    onPrimaryContainer = White,
    secondary = PremiumCyan,
    onSecondary = Color(0xFF08202A),
    secondaryContainer = Color(0xFF163444),
    onSecondaryContainer = White,
    tertiary = PremiumOrange,
    onTertiary = Color(0xFF2A1604),
    tertiaryContainer = Color(0xFF3B2A1A),
    onTertiaryContainer = White,
    background = PremiumBackgroundTop,
    onBackground = Color(0xFFE7ECF2),
    surface = Color(0xFF121A24),
    onSurface = Color(0xFFE7ECF2),
    surfaceVariant = PremiumSurfaceSoft,
    onSurfaceVariant = Color(0xFFB9C5D4),
    surfaceDim = Color(0xFF0E151E),
    surfaceBright = Color(0xFF1A2432),
    surfaceContainerLowest = Color(0xFF101822),
    surfaceContainerLow = Color(0xFF15202C),
    surfaceContainer = Color(0xFF182333),
    surfaceContainerHigh = Color(0xFF1E2A3B),
    surfaceContainerHighest = Color(0xFF243246),
    error = PremiumDanger,
    onError = Color(0xFF260505),
    errorContainer = Color(0xFF4A1D1D),
    onErrorContainer = Color(0xFFFFDADA),
    outline = PremiumStroke,
    outlineVariant = Color(0xFF223142),
    scrim = Color(0xFF04080D)
)

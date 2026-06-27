package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsDarkTheme = staticCompositionLocalOf { false }

// Modern High-Energy Sports Brand Palette (X SPORT)
val SportOrange = Color(0xFFFF5500) // Energetic Sports Neon Orange
val SportBlue = Color(0xFF00B0FF)   // Sleek High-Contrast Sky Blue
val SportAmber = Color(0xFFFFC107)  // Champion Luminous Gold
val SportGold = Color(0xFFFFD700)   // Gold Badge Accent
val TraditionalTeal = Color(0xFF10B981) // Live Active Emerald Green

val DarkBackground: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF090A10) else Color(0xFFF8FAFC)

val CardBackground: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF141722) else Color(0xFFFFFFFF)

val LightText: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFFF1F5F9) else Color(0xFF0F172A)

val MutedText: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF94A3B8) else Color(0xFF64748B)

val BorderColor: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF222636) else Color(0xFFE2E8F0)

val Purple80 = Color(0xFFC084FC)
val PurpleGrey80 = Color(0xFFCBD5E1)
val Pink80 = Color(0xFFF472B6)

val Purple40 = Color(0xFF7E22CE)
val PurpleGrey40 = Color(0xFF475569)
val Pink40 = Color(0xFFDB2777)

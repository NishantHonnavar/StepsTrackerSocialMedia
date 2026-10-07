package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Dark Neon Minimalism Design Tokens
val DeepVoidNavy = Color(0xFF0B0F17) // Background
val DarkSlateNavy = Color(0xFF141C28) // Surface / Cards
val InnerCardBorder = Color(0xFF1F2A3D) // 1dp Inner Card Border

// Accents
val ElectricCyan = Color(0xFF00E5FF) // Primary Brand
val WarmAmberGold = Color(0xFFFFAA00) // Golden Hour
val DeepLavender = Color(0xFF7C4DFF) // Night Surge
val CrimsonCoral = Color(0xFFFF4B6E) // Warning / Locked State

// Neutral Text Hierarchy
val TextPrimary = Color(0xFFFFFFFF) // Primary
val TextSecondary = Color(0xFF94A3B8) // Secondary
val TextMuted = Color(0xFF64748B) // Muted

// Compatibility tokens for existing screens & themes
val BackgroundDark = DeepVoidNavy
val SurfaceDark = DarkSlateNavy
val SurfaceVariantDark = InnerCardBorder
val TextPrimaryDark = TextPrimary
val TextSecondaryDark = TextSecondary
val TextMutedDark = TextMuted
val CyanAccent = ElectricCyan
val CyanDeep = Color(0xFF0284C7)
val BlueDark = DeepVoidNavy
val BlueCard = DarkSlateNavy
val BlueCardLight = InnerCardBorder
val LockRed = CrimsonCoral
val LockRedGlow = Color(0x33FF4B6E)
val UnlockGreen = Color(0xFF00E699)
val EmeraldNeon = Color(0xFF00E699)
val WarningAmber = WarmAmberGold

// Light Theme Fallbacks
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)

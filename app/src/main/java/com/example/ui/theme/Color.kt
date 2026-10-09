package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Obsidian Black & Deep Neon Purple Design Tokens
val DeepVoidNavy = Color(0xFF090611) // Deep Obsidian / Black Background
val DarkSlateNavy = Color(0xFF140D24) // Deep Obsidian Purple Surface / Cards
val InnerCardBorder = Color(0xFF281A42) // 1dp Inner Card Border / Purple Glow Border

// Accents (Purple & Electric Violet)
val ElectricCyan = Color(0xFFA855F7) // Vibrant Royal Purple / Primary Brand
val VividPurple = Color(0xFF9333EA) // Deep Electric Purple
val DeepLavender = Color(0xFFC084FC) // Soft Neon Lavender
val NeonViolet = Color(0xFFD946EF) // Magenta Violet Accent
val WarmAmberGold = Color(0xFFFFB703) // Golden Hour Walking
val CrimsonCoral = Color(0xFFFF3366) // Warning / Locked State

// Neutral Text Hierarchy
val TextPrimary = Color(0xFFFFFFFF) // Crisp Pure White
val TextSecondary = Color(0xFFC4B5FD) // Light Lavender Muted Secondary
val TextMuted = Color(0xFF7E709A) // Deep Muted Purple-Gray

// Compatibility tokens for existing screens & themes
val BackgroundDark = DeepVoidNavy
val SurfaceDark = DarkSlateNavy
val SurfaceVariantDark = InnerCardBorder
val TextPrimaryDark = TextPrimary
val TextSecondaryDark = TextSecondary
val TextMutedDark = TextMuted
val CyanAccent = ElectricCyan
val CyanDeep = Color(0xFF7E22CE)
val BlueDark = DeepVoidNavy
val BlueCard = DarkSlateNavy
val BlueCardLight = InnerCardBorder
val LockRed = CrimsonCoral
val LockRedGlow = Color(0x33FF3366)
val UnlockGreen = Color(0xFF22C55E)
val EmeraldNeon = Color(0xFF22C55E)
val WarningAmber = WarmAmberGold

// Light Theme Fallbacks
val BackgroundLight = Color(0xFFFAF7FD)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF3E8FF)
val TextPrimaryLight = Color(0xFF1E1138)
val TextSecondaryLight = Color(0xFF6B4E9B)

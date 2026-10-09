package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan, // Purple Accent
    onPrimary = TextPrimary,
    primaryContainer = CyanDeep,
    onPrimaryContainer = TextPrimaryDark,
    secondary = DeepLavender,
    onSecondary = DeepVoidNavy,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = NeonViolet,
    background = DeepVoidNavy,
    onBackground = TextPrimaryDark,
    surface = DarkSlateNavy,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    error = LockRed,
    onError = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDeep,
    onPrimary = TextPrimaryDark,
    primaryContainer = ElectricCyan,
    onPrimaryContainer = DeepVoidNavy,
    secondary = VividPurple,
    onSecondary = TextPrimaryDark,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = NeonViolet,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    error = LockRed,
    onError = TextPrimaryDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Black & Purple dark theme by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

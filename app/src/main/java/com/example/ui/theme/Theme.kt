package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WarmColorScheme =
  lightColorScheme(
    primary = EarthBrownDark,
    onPrimary = WarmIvorySurface,
    primaryContainer = EarthBrownLight,
    onPrimaryContainer = DarkSoil,
    secondary = EarthBrown,
    onSecondary = WarmIvorySurface,
    secondaryContainer = CreamMuted,
    onSecondaryContainer = DarkSoil,
    tertiary = EarthBrownDark,
    onTertiary = WarmIvorySurface,
    background = WarmIvoryBg,
    onBackground = DarkSoil,
    surface = WarmIvorySurface,
    onSurface = DarkSoil,
    surfaceVariant = CreamMuted,
    onSurfaceVariant = DarkSoil,
  )

@Composable
fun SaamparanTheme(
  content: @Composable () -> Unit,
) {
  // Always use the customized warm color scheme to obey the warm tones and no dark backgrounds rule.
  MaterialTheme(
    colorScheme = WarmColorScheme,
    typography = Typography,
    content = content
  )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = BounceRed,
  onPrimary = Color.White,
  primaryContainer = BounceRedDark,
  onPrimaryContainer = Color.White,
  secondary = RingGold,
  onSecondary = Color.Black,
  secondaryContainer = RingAmber,
  onSecondaryContainer = Color.Black,
  tertiary = RetroCyan,
  onTertiary = Color.Black,
  background = RetroDarkBg,
  onBackground = Color.White,
  surface = RetroSurface,
  onSurface = Color.White,
  surfaceVariant = RetroSurfaceLight,
  onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = BounceRed,
  onPrimary = Color.White,
  primaryContainer = BounceRedLight,
  onPrimaryContainer = BounceRedDark,
  secondary = RingAmber,
  onSecondary = Color.Black,
  secondaryContainer = RingGoldLight,
  onSecondaryContainer = Color.Black,
  tertiary = RetroCyan,
  onTertiary = Color.Black,
  background = RetroDarkBg,
  onBackground = Color.White,
  surface = RetroSurface,
  onSurface = Color.White,
  surfaceVariant = RetroSurfaceLight,
  onSurfaceVariant = Color.White
)

@Composable
fun BounceTalesTheme(
  darkTheme: Boolean = true, // Arcade theme defaults to immersive dark
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ImmersiveColorScheme = darkColorScheme(
  primary = ImmersiveGoldBright,
  onPrimary = ImmersiveDarkBg,
  primaryContainer = ImmersiveGoldDark,
  onPrimaryContainer = ImmersiveGoldLight,
  secondary = ImmersiveEmeraldLight,
  onSecondary = ImmersiveDarkBg,
  secondaryContainer = ImmersiveFeltGreen,
  onSecondaryContainer = Color.White,
  tertiary = ImmersiveCrimsonLight,
  onTertiary = Color.White,
  background = ImmersiveDarkBg,
  onBackground = TextLight,
  surface = ImmersiveDeepGreen,
  onSurface = TextLight,
  surfaceVariant = ImmersiveSurfaceCard,
  onSurfaceVariant = ImmersiveGoldLight
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ImmersiveColorScheme,
    typography = Typography,
    content = content
  )
}



package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BrixLeafDarkColorScheme = darkColorScheme(
    primary = ChlorophyllEmerald,
    onPrimary = ObsidianVoid,
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = MintInk,
    secondary = NectarAmber,
    onSecondary = ObsidianVoid,
    secondaryContainer = GlassCardElevated,
    onSecondaryContainer = NectarAmber,
    tertiary = CrystalCyan,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = MintInk,
    surface = ObsidianSurface,
    onSurface = MintInk,
    surfaceVariant = GlassCard,
    onSurfaceVariant = SageSecondary,
    outline = GlassStroke,
    error = BitterCoral,
    onError = ObsidianVoid
)

@Composable
fun BrixLeafTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = BrixLeafDarkColorScheme,
        typography = BrixLeafTypography,
        content = content
    )
}

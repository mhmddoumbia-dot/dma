package com.dma.finance.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = NeutralSurfaceLight,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenPrimaryDark,
    secondary = GreenPrimaryLight,
    background = NeutralBackgroundLight,
    surface = NeutralSurfaceLight,
    error = ExpenseRed
)

private val DarkColors = darkColorScheme(
    primary = GreenContainer,
    onPrimary = GreenPrimaryDark,
    primaryContainer = GreenPrimaryDark,
    onPrimaryContainer = GreenContainer,
    secondary = GreenPrimaryLight,
    background = NeutralBackgroundDark,
    surface = NeutralSurfaceDark,
    error = ExpenseRed
)

@Composable
fun GestionFinancesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

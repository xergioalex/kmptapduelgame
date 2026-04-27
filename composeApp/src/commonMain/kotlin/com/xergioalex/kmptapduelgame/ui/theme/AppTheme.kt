package com.xergioalex.kmptapduelgame.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val playerOneBlue = Color(0xFF1976D2)
private val playerOneBlueDark = Color(0xFF82B1FF)
private val playerOneSurface = Color(0xFFBBDEFB)
private val playerOneSurfaceDark = Color(0xFF0D47A1)

private val playerTwoRed = Color(0xFFD32F2F)
private val playerTwoRedDark = Color(0xFFFF8A80)
private val playerTwoSurface = Color(0xFFFFCDD2)
private val playerTwoSurfaceDark = Color(0xFFB71C1C)

private val lightScheme = lightColorScheme(
    primary = playerOneBlue,
    onPrimary = Color.White,
    primaryContainer = playerOneSurface,
    onPrimaryContainer = Color(0xFF0D2B4D),
    error = playerTwoRed,
    onError = Color.White,
    errorContainer = playerTwoSurface,
    onErrorContainer = Color(0xFF4D0D0D),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF333333),
)

private val darkScheme = darkColorScheme(
    primary = playerOneBlueDark,
    onPrimary = Color(0xFF0A1F33),
    primaryContainer = playerOneSurfaceDark,
    onPrimaryContainer = Color(0xFFE3F2FD),
    error = playerTwoRedDark,
    onError = Color(0xFF330A0A),
    errorContainer = playerTwoSurfaceDark,
    onErrorContainer = Color(0xFFFFEBEE),
    background = Color(0xFF0B0B0B),
    onBackground = Color(0xFFEFEFEF),
    surface = Color(0xFF161616),
    onSurface = Color(0xFFEFEFEF),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFD4D4D4),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) darkScheme else lightScheme,
        content = content,
    )
}

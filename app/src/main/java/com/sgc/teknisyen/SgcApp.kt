package com.sgc.teknisyen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SgcTeknisyenTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

private val lightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF0B3B60),
    primaryContainer = Color(0xFF1976D2),
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF1976D2),
    secondaryContainer = Color(0xFF1976D2),
    onSecondary = Color.White,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF1976D2),
    tertiaryContainer = Color(0xFF1976D2),
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF49454F),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F)
)

private val darkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF1976D2),
    primaryContainer = Color(0xFF0B3B60),
    onPrimary = Color.Black,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF1976D2),
    secondaryContainer = Color(0xFF0B3B60),
    onSecondary = Color.Black,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF1976D2),
    tertiaryContainer = Color(0xFF0B3B60),
    onTertiary = Color.Black,
    error = Color(0xFFFFB4AB),
    onError = Color.Black,
    surface = Color(0xFF1C1B1F),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    background = Color(0xFF1C1B1F),
    onBackground = Color.White
)

val Typography = androidx.compose.material3.Typography(
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

val Shapes = androidx.compose.material3.Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
)
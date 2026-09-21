package com.example.zivaministries.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.zivaministries.ui.theme.Typography


val ZivaTypography = Typography(
    // Display styles (largest)
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles (main content)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles (buttons, captions)
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)


@Stable
object ZivaColors {
    val green = Green80
    val greenDark = Green90
    val greenLight = Green60
    val gold = Gold80
    val goldDark = Gold90
    val goldLight = Gold60
}
// 1. Define your brand colors as constants
val Green80 = Color(0xFF2E7D32)      // Primary Green
val Green90 = Color(0xFF1B5E20)      // Dark Green
val Green60 = Color(0xFF4CAF50)      // Light Green
val Green40 = Color(0xFF388E3C)      // Medium Green

val Gold80 = Color(0xFFFFD700)       // Primary Gold
val Gold90 = Color(0xFFF9A825)       // Dark Gold
val Gold60 = Color(0xFFFFEE58)

// 1. Gold Brand Colors
val GoldPrimary = Color(0xFFC9A84C)      // Main gold
val GoldDark = Color(0xFFA8873B)         // Darker gold (for pressed states)
val GoldLight = Color(0xFFE8D5A3)        // Light gold (for backgrounds)
val GoldVeryLight = Color(0xFFF5EDD6)    // Very light gold
val GoldAccent = Color(0xFFFFD700)       // Brighter gold for accents

// 2. Neutral Colors
val White = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF5F5F5)
val DarkGray = Color(0xFF1A1A1A)
val MediumGray = Color(0xFF757575)
val LightGray = Color(0xFFE0E0E0)// Light Gold

// Neutral colors
val Grey95 = Color(0xFF121212)       // Almost black (dark mode)
val Grey90 = Color(0xFF1E1E1E)
val Grey80 = Color(0xFF424242)
val Grey50 = Color(0xFF757575)
val Grey20 = Color(0xFFE0E0E0)
val Grey5 = Color(0xFFF5F5F5)        // Almost white

private val DarkColorScheme = darkColorScheme(
    primary = GoldLight,                    // Lighter green in dark mode
    onPrimary = DarkGray,
    primaryContainer = GoldPrimary,
    onPrimaryContainer = White,

    secondary = GoldLight,                   // Lighter gold in dark mode
    onSecondary = DarkGray,
    secondaryContainer = GoldPrimary,
    onSecondaryContainer = White,

    background = Color(0xFF121212),                  // Dark background
    onBackground = White,

    surface = Color(0xFF1E1E1E),                     // Dark cards
    onSurface = White,

    error = Color(0xFFEF5350),
    onError = DarkGray,
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,                    // Main brand color
    onPrimary = Color.White,              // Text/icons on primary
    primaryContainer = GoldLight,           // Background for primary elements
    onPrimaryContainer = DarkGray,     // Text on primaryContainer

    secondary = GoldPrimary,                   // Accent color
    onSecondary = White,            // Text on secondary
    secondaryContainer = GoldLight,          // Background for secondary
    onSecondaryContainer = DarkGray,   // Text on secondaryContainer

    background = OffWhite,                   // Main background
    onBackground = DarkGray,                // Text on background

    surface = Color.White,                // Card backgrounds
    onSurface = DarkGray,                   // Text on cards

    error = Color(0xFFD32F2F),            // Error color
    onError = Color.White,
)

@Composable
fun ZivaMinistriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }

    }

        MaterialTheme(
        colorScheme = colorScheme,
        typography = ZivaTypography,
        content = content
    )
}
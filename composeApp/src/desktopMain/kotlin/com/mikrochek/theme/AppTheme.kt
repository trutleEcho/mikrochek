package com.mikrochek.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    // Primary Colors - Modern Blue (less dark than before)
    val Primary = Color(0xFF3B82F6)         // Vibrant Blue
    val PrimaryLight = Color(0xFF60A5FA)    // Light Blue
    val PrimaryDark = Color(0xFF2563EB)     // Dark Blue
    
    // Secondary Colors - Complementary Teal
    val Secondary = Color(0xFF0EA5E9)       // Bright Teal
    val SecondaryLight = Color(0xFF38BDF8)  // Light Teal
    val SecondaryDark = Color(0xFF0284C7)   // Dark Teal
    
    // Accent Colors for UI Elements
    val Accent1 = Color(0xFF8B5CF6)         // Purple
    val Accent2 = Color(0xFFF97316)         // Orange
    val Accent3 = Color(0xFF10B981)         // Emerald
    val Accent4 = Color(0xFFEF4444)         // Red
    val Accent5 = Color(0xFFF59E0B)         // Amber

    // Neutral Colors - Cool Gray
    val Gray50 = Color(0xFFF8FAFC)          // Almost White
    val Gray100 = Color(0xFFF1F5F9)         // Very Light Gray
    val Gray200 = Color(0xFFE2E8F0)         // Light Gray
    val Gray300 = Color(0xFFCBD5E1)         // Gray
    val Gray400 = Color(0xFF94A3B8)         // Medium Gray
    val Gray500 = Color(0xFF64748B)         // Medium Dark Gray
    val Gray600 = Color(0xFF475569)         // Dark Gray
    val Gray700 = Color(0xFF334155)         // Very Dark Gray
    val Gray800 = Color(0xFF1E293B)         // Almost Black
    val Gray900 = Color(0xFF0F172A)         // Black

    // Light Theme
    val Background = Gray50
    val Surface = Color.White
    val SurfaceVariant = Gray100
    val OnPrimary = Color.White
    val OnSecondary = Color.White
    val OnBackground = Gray900
    val OnSurface = Gray800
    val OnSurfaceVariant = Gray600

    // Dark Theme
    val DarkBackground = Gray900
    val DarkSurface = Gray800
    val DarkSurfaceVariant = Gray700
    val DarkOnPrimary = Color.White
    val DarkOnSecondary = Color.White
    val DarkOnBackground = Gray100
    val DarkOnSurface = Gray400
    val DarkOnSurfaceVariant = Gray400

    // Semantic Colors
    val Success = Color(0xFF22C55E)         // Green
    val SuccessLight = Color(0xFFDCFCE7)    // Light Green Background
    val Warning = Color(0xFFF59E0B)         // Amber
    val WarningLight = Color(0xFFFEF3C7)    // Light Amber Background
    val Error = Color(0xFFEF4444)           // Red
    val ErrorLight = Color(0xFFFEE2E2)      // Light Red Background
    val Info = Color(0xFF3B82F6)            // Blue
    val InfoLight = Color(0xFFDBEAFE)       // Light Blue Background
}

object AppTypography {
    val h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        letterSpacing = (-1.5).sp
    )
    
    val h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 60.sp,
        letterSpacing = (-0.5).sp
    )
    
    val h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        letterSpacing = 0.sp
    )
    
    val h4 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        letterSpacing = 0.25.sp
    )
    
    val h5 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    )
    
    val h6 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        letterSpacing = 0.15.sp
    )
    
    val subtitle1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    )
    
    val subtitle2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    )
    
    val body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    )
    
    val body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    )
    
    val button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.25.sp
    )
    
    val caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    )
    
    val overline = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
    )
}

private val DarkColorPalette = darkColors(
    primary = AppColors.Primary,
    primaryVariant = AppColors.PrimaryDark,
    secondary = AppColors.Secondary,
    secondaryVariant = AppColors.SecondaryDark,
    background = AppColors.DarkBackground,
    surface = AppColors.DarkSurface,
    error = AppColors.Error,
    onPrimary = AppColors.DarkOnPrimary,
    onSecondary = AppColors.DarkOnSecondary,
    onBackground = AppColors.DarkOnBackground,
    onSurface = AppColors.DarkOnSurface,
    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = AppColors.Primary,
    primaryVariant = AppColors.PrimaryDark,
    secondary = AppColors.Secondary,
    secondaryVariant = AppColors.SecondaryDark,
    background = AppColors.Background,
    surface = AppColors.Surface,
    error = AppColors.Error,
    onPrimary = AppColors.OnPrimary,
    onSecondary = AppColors.OnSecondary,
    onBackground = AppColors.OnBackground,
    onSurface = AppColors.OnSurface,
    onError = Color.White
)

object AppShapes {
    val small = Shapes(
        small = RoundedCornerShape(6.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography(
            h1 = AppTypography.h1,
            h2 = AppTypography.h2,
            h3 = AppTypography.h3,
            h4 = AppTypography.h4,
            h5 = AppTypography.h5,
            h6 = AppTypography.h6,
            subtitle1 = AppTypography.subtitle1,
            subtitle2 = AppTypography.subtitle2,
            body1 = AppTypography.body1,
            body2 = AppTypography.body2,
            button = AppTypography.button,
            caption = AppTypography.caption,
            overline = AppTypography.overline
        ),
        shapes = AppShapes.small,
        content = content
    )
} 
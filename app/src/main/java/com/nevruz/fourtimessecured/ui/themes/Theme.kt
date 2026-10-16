package com.nevruz.fourtimessecured.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class ZenithThemeMode(val title: String, val description: String) {
    SYSTEM("Sistem", "Telefonun açık veya koyu görünümünü takip eder"),
    LIGHT("Aydınlık", "Temiz mint yüzeyler, yumuşak gölgeler"),
    DARK("Karanlık", "Grafit derinlik ve zümrüt vurgu"),
    LIQUID_GLASS("Liquid Glass", "Buzlu cam, ışık ve yarı saydam katmanlar"),
    MATERIAL_YOU("Material You", "Yumuşak Android yüzeyleri ve Google Sans"),
    AURORA("Aurora", "Kuzey ışığı: mor sis ve turkuaz parıltı");

    companion object {
        fun fromName(name: String?): ZenithThemeMode =
            values().firstOrNull { it.name == name } ?: SYSTEM
    }
}

val LocalZenithThemeMode = staticCompositionLocalOf { ZenithThemeMode.SYSTEM }

val Emerald = Color(0xFF10B981)
val EmeraldLight = Color(0xFF6EE7B7)
val DarkBackground = Color(0xFF070A0F)
val CardSurface = Color(0xFF12181F)
val AccentCyan = Color(0xFF54E4D1)
val AccentPurple = Color(0xFF9D8CFF)
val StatusGreen = Color(0xFF34D399)
val StatusWarning = Color(0xFFFBBF24)

private fun fontForTheme(@Suppress("UNUSED_PARAMETER") themeMode: ZenithThemeMode): FontFamily =
    FontFamily.SansSerif

private val ProtectShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun ZenithGuardTheme(
    themeMode: ZenithThemeMode = ZenithThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val lightPalette = lightColorScheme(
        primary = Color(0xFF0B8A66),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC8F6E4),
        onPrimaryContainer = Color(0xFF003827),
        secondary = Color(0xFF3E6B5C),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD5EDE3),
        onSecondaryContainer = Color(0xFF143328),
        tertiary = Color(0xFF3B6EA8),
        onTertiary = Color.White,
        background = Color(0xFFF4F8F6),
        onBackground = Color(0xFF12201A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF12201A),
        surfaceVariant = Color(0xFFE3EEE8),
        onSurfaceVariant = Color(0xFF4E6258),
        outline = Color(0xFFC5D5CC),
        error = Color(0xFFB42318),
        errorContainer = Color(0xFFFFE4E1),
        onError = Color.White
    )
    val darkPalette = darkColorScheme(
        primary = Emerald,
        onPrimary = Color(0xFF032018),
        primaryContainer = Color(0xFF14543F),
        onPrimaryContainer = Color(0xFFB6F5D8),
        secondary = EmeraldLight,
        onSecondary = Color(0xFF073326),
        secondaryContainer = Color(0xFF1C2E28),
        onSecondaryContainer = Color(0xFFCDE9DC),
        tertiary = AccentCyan,
        onTertiary = Color(0xFF00332C),
        background = DarkBackground,
        onBackground = Color(0xFFE8F2ED),
        surface = CardSurface,
        onSurface = Color(0xFFE8F2ED),
        surfaceVariant = Color(0xFF1C252C),
        onSurfaceVariant = Color(0xFFAFC0B7),
        outline = Color(0xFF34433C),
        error = Color(0xFFFF6B6B),
        errorContainer = Color(0xFF5A1B1B),
        onError = Color.White
    )

    val colors = when (themeMode) {
        ZenithThemeMode.SYSTEM -> if (systemDark) darkPalette else lightPalette
        ZenithThemeMode.LIGHT -> lightPalette
        ZenithThemeMode.DARK -> darkPalette
        ZenithThemeMode.LIQUID_GLASS -> darkColorScheme(
            primary = Color(0xFF9BF7DE),
            onPrimary = Color(0xFF04261F),
            primaryContainer = Color(0x6659DCC0),
            onPrimaryContainer = Color(0xFFE8FFF8),
            secondary = Color(0xFFB5B8FF),
            onSecondary = Color(0xFF17183D),
            secondaryContainer = Color(0x663B4A78),
            onSecondaryContainer = Color(0xFFE4E7FF),
            tertiary = Color(0xFF7ED0FF),
            background = Color(0xFF061018),
            onBackground = Color(0xFFF1FAFF),
            surface = Color(0xB81A2B3B),
            onSurface = Color(0xFFF1FAFF),
            surfaceVariant = Color(0x993B5263),
            onSurfaceVariant = Color(0xFFD0E4EF),
            outline = Color(0x6689B6C9),
            error = Color(0xFFFF8A9A)
        )
        ZenithThemeMode.MATERIAL_YOU -> darkColorScheme(
            primary = Color(0xFFB4E8C9),
            onPrimary = Color(0xFF173829),
            primaryContainer = Color(0xFF315442),
            onPrimaryContainer = Color(0xFFD0F5DF),
            secondary = Color(0xFFB9DCC8),
            onSecondary = Color(0xFF20392B),
            secondaryContainer = Color(0xFF24332B),
            onSecondaryContainer = Color(0xFFD5EBDD),
            tertiary = Color(0xFFA7C8FF),
            background = Color(0xFF101512),
            onBackground = Color(0xFFE2E9E2),
            surface = Color(0xFF191F1B),
            onSurface = Color(0xFFE2E9E2),
            surfaceVariant = Color(0xFF29332D),
            onSurfaceVariant = Color(0xFFC0CCC3),
            outline = Color(0xFF46534A)
        )
        ZenithThemeMode.AURORA -> darkColorScheme(
            primary = Color(0xFF80F5DC),
            onPrimary = Color(0xFF052C29),
            primaryContainer = Color(0xFF174A53),
            onPrimaryContainer = Color(0xFFC5FFF3),
            secondary = Color(0xFFC1A7FF),
            onSecondary = Color(0xFF281A4A),
            secondaryContainer = Color(0xFF2A2150),
            onSecondaryContainer = Color(0xFFE6DCFF),
            tertiary = Color(0xFFFF9AD5),
            background = Color(0xFF070A18),
            onBackground = Color(0xFFF1F1FF),
            surface = Color(0xFF13182B),
            onSurface = Color(0xFFF1F1FF),
            surfaceVariant = Color(0xFF222844),
            onSurfaceVariant = Color(0xFFC5CBE8),
            outline = Color(0xFF454D72)
        )
    }

    val fontFamily = fontForTheme(themeMode)
    val baseTypography = Typography()
    val typography = baseTypography.copy(
        displayLarge = baseTypography.displayLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        displayMedium = baseTypography.displayMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        displaySmall = baseTypography.displaySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        headlineLarge = baseTypography.headlineLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        headlineMedium = baseTypography.headlineMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        headlineSmall = baseTypography.headlineSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        titleSmall = baseTypography.titleSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        labelMedium = baseTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontFamily = fontFamily)
    )

    val lightBars = when (themeMode) {
        ZenithThemeMode.LIGHT -> true
        ZenithThemeMode.SYSTEM -> !systemDark
        else -> false
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightBars
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = lightBars
        }
    }

    CompositionLocalProvider(LocalZenithThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = ProtectShapes,
            content = content
        )
    }
}

package com.nevruz.fourtimessecured.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.nevruz.fourtimessecured.R

enum class ZenithThemeMode(val title: String, val description: String) {
    SYSTEM("Sistem", "Telefonun açık/koyu modunu takip eder"),
    LIGHT("Aydınlık", "Temiz, açık yüzeyler ve zümrüt vurgular"),
    DARK("Zenith Dark", "Grafit yüzeyler ve zümrüt vurgular"),
    LIQUID_GLASS("Liquid Glass", "Cam hissi veren yarı saydam katmanlar"),
    MATERIAL_YOU("Material You", "Yumuşak Android yüzeyleri ve Google Sans"),
    AURORA("Aurora", "Kuzey ışıklarından ilham alan mor ve turkuaz tonlar");

    companion object {
        fun fromName(name: String?): ZenithThemeMode =
            values().firstOrNull { it.name == name } ?: SYSTEM
    }
}

val LocalZenithThemeMode = staticCompositionLocalOf { ZenithThemeMode.SYSTEM }

val Emerald = Color(0xFF10B981)
val EmeraldLight = Color(0xFF6EE7B7)
val DarkBackground = Color(0xFF090D12)
val CardSurface = Color(0xFF151C22)
val AccentCyan = Color(0xFF54E4D1)
val AccentPurple = Color(0xFF9D8CFF)
val StatusGreen = Color(0xFF34D399)
val StatusWarning = Color(0xFFFBBF24)

private val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold)
)

private fun fontForTheme(themeMode: ZenithThemeMode): FontFamily = when (themeMode) {
    ZenithThemeMode.MATERIAL_YOU -> GoogleSansFlex
    ZenithThemeMode.LIQUID_GLASS -> FontFamily(Typeface.create("sans-serif", Typeface.NORMAL))
    ZenithThemeMode.SYSTEM,
    ZenithThemeMode.LIGHT,
    ZenithThemeMode.DARK,
    ZenithThemeMode.AURORA -> FontFamily(Typeface.create("sans-serif-medium", Typeface.NORMAL))
}

@Composable
fun ZenithGuardTheme(
    themeMode: ZenithThemeMode = ZenithThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val lightPalette = lightColorScheme(
        primary = Color(0xFF087F5B),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC8F7E5),
        onPrimaryContainer = Color(0xFF003B2A),
        secondary = Color(0xFF46675B),
        onSecondary = Color.White,
        background = Color(0xFFF5F8F7),
        onBackground = Color(0xFF17211D),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF17211D),
        surfaceVariant = Color(0xFFE5ECE8),
        onSurfaceVariant = Color(0xFF53645B),
        outline = Color(0xFFCBD8D0)
    )
    val darkPalette = darkColorScheme(
        primary = Emerald,
        onPrimary = Color(0xFF032018),
        primaryContainer = Color(0xFF14543F),
        onPrimaryContainer = Color(0xFFB6F5D8),
        secondary = EmeraldLight,
        onSecondary = Color(0xFF073326),
        background = DarkBackground,
        onBackground = Color(0xFFE8F2ED),
        surface = CardSurface,
        onSurface = Color(0xFFE8F2ED),
        surfaceVariant = Color(0xFF202A31),
        onSurfaceVariant = Color(0xFFB3C2BA),
        outline = Color(0xFF34433C)
    )

    val colors = when (themeMode) {
        ZenithThemeMode.SYSTEM -> if (systemDark) darkPalette else lightPalette
        ZenithThemeMode.LIGHT -> lightPalette
        ZenithThemeMode.DARK -> darkPalette
        ZenithThemeMode.LIQUID_GLASS -> darkColorScheme(
            primary = Color(0xFF9BF7DE),
            onPrimary = Color(0xFF052D24),
            primaryContainer = Color(0x6659DCC0),
            onPrimaryContainer = Color(0xFFE8FFF8),
            secondary = Color(0xFFB5B8FF),
            onSecondary = Color(0xFF17183D),
            background = Color(0xFF07121D),
            onBackground = Color(0xFFF1FAFF),
            surface = Color(0xB81A2B3B),
            onSurface = Color(0xFFF1FAFF),
            surfaceVariant = Color(0x993B5263),
            onSurfaceVariant = Color(0xFFD0E4EF),
            outline = Color(0x6689B6C9)
        )
        ZenithThemeMode.MATERIAL_YOU -> darkColorScheme(
            primary = Color(0xFFB4E8C9),
            onPrimary = Color(0xFF173829),
            primaryContainer = Color(0xFF315442),
            onPrimaryContainer = Color(0xFFD0F5DF),
            secondary = Color(0xFFB9DCC8),
            onSecondary = Color(0xFF20392B),
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
            background = Color(0xFF080C1B),
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
        displayLarge = baseTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = baseTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = baseTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = baseTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = baseTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = baseTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = baseTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = baseTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontFamily = fontFamily)
    )

    CompositionLocalProvider(LocalZenithThemeMode provides themeMode) {
        MaterialTheme(colorScheme = colors, typography = typography, content = content)
    }
}

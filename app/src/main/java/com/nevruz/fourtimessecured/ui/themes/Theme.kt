package com.nevruz.fourtimessecured.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.nevruz.fourtimessecured.R

enum class ZenithThemeMode(val title: String, val description: String) {
    DARK("Zenith Dark", "Grafit yüzeyler ve zümrüt vurgular"),
    LIQUID_GLASS("Liquid Glass", "Yarı saydam cam katmanları"),
    MATERIAL_YOU("Material You", "Android'e özgü yumuşak yüzeyler"),
    WINDOWS_XP("Windows XP Classic", "Klasik mavi pencere görünümü");

    companion object {
        fun fromName(name: String?): ZenithThemeMode =
            values().firstOrNull { it.name == name } ?: DARK
    }
}

val LocalZenithThemeMode = staticCompositionLocalOf { ZenithThemeMode.DARK }

val Emerald = Color(0xFF10B981)
val EmeraldLight = Color(0xFF6EE7B7)
val DarkBackground = Color(0xFF090D12)
val CardSurface = Color(0xFF151C22)
val AccentCyan = Emerald
val AccentPurple = Color(0xFF8B7CFF)
val StatusGreen = Color(0xFF34D399)
val StatusWarning = Color(0xFFFBBF24)

private val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold)
)

/**
 * Google Sans Flex is bundled under the SIL Open Font License.
 * SF Pro is not bundled because Apple's current guidance says not to ship it
 * inside apps; Liquid Glass uses Android's standard sans-serif instead.
 * SamsungOne is requested for Samsung-style themes and gracefully falls back
 * to Android's default sans-serif on devices where it is unavailable.
 */
private fun fontForTheme(themeMode: ZenithThemeMode): FontFamily = when (themeMode) {
    ZenithThemeMode.MATERIAL_YOU -> GoogleSansFlex
    ZenithThemeMode.LIQUID_GLASS -> FontFamily(Typeface.create("sans-serif", Typeface.NORMAL))
    ZenithThemeMode.DARK,
    ZenithThemeMode.WINDOWS_XP -> FontFamily(Typeface.create("SamsungOne", Typeface.NORMAL))
}

@Composable
fun ZenithGuardTheme(
    themeMode: ZenithThemeMode = ZenithThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colors = when (themeMode) {
        ZenithThemeMode.DARK -> darkColorScheme(
            primary = Emerald,
            onPrimary = Color(0xFF032018),
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
        ZenithThemeMode.LIQUID_GLASS -> darkColorScheme(
            primary = EmeraldLight,
            onPrimary = Color(0xFF06271C),
            secondary = Color(0xFF8ADBC1),
            onSecondary = Color(0xFF06271C),
            background = Color(0xFF08111A),
            onBackground = Color(0xFFF0FBF7),
            surface = Color(0xB81D2A32),
            onSurface = Color(0xFFF0FBF7),
            surfaceVariant = Color(0x992C4147),
            onSurfaceVariant = Color(0xFFD0E3DC),
            outline = Color(0x6687B5A4)
        )
        ZenithThemeMode.MATERIAL_YOU -> darkColorScheme(
            primary = Emerald,
            onPrimary = Color(0xFF002116),
            secondary = Color(0xFFB2DCCB),
            onSecondary = Color(0xFF18382A),
            background = Color(0xFF101512),
            onBackground = Color(0xFFE2E9E2),
            surface = Color(0xFF191F1B),
            onSurface = Color(0xFFE2E9E2),
            surfaceVariant = Color(0xFF29332D),
            onSurfaceVariant = Color(0xFFC0CCC3),
            outline = Color(0xFF46534A)
        )
        ZenithThemeMode.WINDOWS_XP -> lightColorScheme(
            primary = Emerald,
            onPrimary = Color(0xFF032018),
            secondary = Color(0xFF1769C2),
            onSecondary = Color.White,
            background = Color(0xFFE8F0F8),
            onBackground = Color(0xFF182A3A),
            surface = Color(0xFFF8FBFF),
            onSurface = Color(0xFF182A3A),
            surfaceVariant = Color(0xFFD7E5F4),
            onSurfaceVariant = Color(0xFF344E67),
            outline = Color(0xFF8DAAC6)
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
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = content
        )
    }
}

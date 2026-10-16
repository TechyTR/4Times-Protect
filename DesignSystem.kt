package com.nevruz.fourtimessecured.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon

data class ProtectSurfaceTokens(
    val shape: RoundedCornerShape,
    val container: Color,
    val border: Color?,
    val elevation: Dp
)

data class StatusTone(
    val color: Color,
    val title: String,
    val progress: Float
)

@Composable
fun rememberProtectSurfaceTokens(): ProtectSurfaceTokens {
    val mode = LocalZenithThemeMode.current
    val scheme = MaterialTheme.colorScheme
    val radius = when (mode) {
        ZenithThemeMode.LIQUID_GLASS -> 28.dp
        ZenithThemeMode.MATERIAL_YOU -> 26.dp
        ZenithThemeMode.AURORA -> 30.dp
        ZenithThemeMode.LIGHT -> 24.dp
        else -> 22.dp
    }
    return ProtectSurfaceTokens(
        shape = RoundedCornerShape(radius),
        container = when (mode) {
            ZenithThemeMode.LIQUID_GLASS -> Color(0xCC163044)
            ZenithThemeMode.AURORA -> Color(0xE6161C38)
            else -> scheme.surface
        },
        border = when (mode) {
            ZenithThemeMode.LIQUID_GLASS -> Color(0x6698D7EA)
            ZenithThemeMode.AURORA -> Color(0x55C1A7FF)
            ZenithThemeMode.LIGHT -> scheme.outline.copy(alpha = 0.55f)
            else -> scheme.outline.copy(alpha = 0.28f)
        },
        elevation = when (mode) {
            ZenithThemeMode.LIQUID_GLASS, ZenithThemeMode.AURORA -> 0.dp
            ZenithThemeMode.LIGHT -> 1.dp
            else -> 3.dp
        }
    )
}

fun ZenithThemeMode.previewSwatches(): List<Color> = when (this) {
    ZenithThemeMode.SYSTEM -> listOf(Color(0xFF10B981), Color(0xFFF5F8F7), Color(0xFF090D12))
    ZenithThemeMode.LIGHT -> listOf(Color(0xFF0F9F75), Color(0xFFF7FBFA), Color(0xFFD7F4E8))
    ZenithThemeMode.DARK -> listOf(Color(0xFF10B981), Color(0xFF090D12), Color(0xFF151C22))
    ZenithThemeMode.LIQUID_GLASS -> listOf(Color(0xFF9BF7DE), Color(0xFF07121D), Color(0xFF54E4D1))
    ZenithThemeMode.MATERIAL_YOU -> listOf(Color(0xFFB4E8C9), Color(0xFF101512), Color(0xFF315442))
    ZenithThemeMode.AURORA -> listOf(Color(0xFF80F5DC), Color(0xFF080C1B), Color(0xFFC1A7FF))
}

fun statusToneFor(status: String): StatusTone = when (status) {
    "SCANNING" -> StatusTone(Color(0xFF5B8DEF), "Tarama sürüyor", 0.62f)
    "SCANNED" -> StatusTone(Color(0xFF22C55E), "Bilinen tehdit yok", 1f)
    "DANGER" -> StatusTone(Color(0xFFE11D48), "Tehlike tespiti", 0.22f)
    "REVIEW" -> StatusTone(Color(0xFFF59E0B), "İnceleme gerekiyor", 0.48f)
    else -> StatusTone(Color(0xFFD97706), "Henüz taranmadı", 0.16f)
}

@Composable
fun AmbientBackdrop(
    themeMode: ZenithThemeMode,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val glowA = when (themeMode) {
        ZenithThemeMode.AURORA -> Color(0xFF7C5CFF)
        ZenithThemeMode.LIQUID_GLASS -> Color(0xFF3EE0C8)
        ZenithThemeMode.LIGHT -> Color(0xFF8EE7C8)
        ZenithThemeMode.MATERIAL_YOU -> Color(0xFF6FBF93)
        else -> scheme.primary
    }
    val glowB = when (themeMode) {
        ZenithThemeMode.AURORA -> Color(0xFF2EE6C7)
        ZenithThemeMode.LIQUID_GLASS -> Color(0xFF6EA8FF)
        ZenithThemeMode.LIGHT -> Color(0xFFB8D4C8)
        else -> scheme.secondary
    }
    val gradient = when (themeMode) {
        ZenithThemeMode.AURORA -> listOf(Color(0xFF141A38), Color(0xFF080C1B), Color(0xFF10242C))
        ZenithThemeMode.LIQUID_GLASS -> listOf(Color(0xFF123044), Color(0xFF07121D), Color(0xFF142536))
        ZenithThemeMode.LIGHT -> listOf(Color(0xFFF8FBFA), Color(0xFFEEF6F2), Color(0xFFE7F3EE))
        ZenithThemeMode.MATERIAL_YOU -> listOf(Color(0xFF141A16), Color(0xFF101512), Color(0xFF18201B))
        else -> listOf(scheme.background, scheme.background, scheme.background.copy(alpha = 0.96f))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowA.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width * 0.86f, size.height * 0.08f),
                    radius = size.minDimension * 0.72f
                ),
                radius = size.minDimension * 0.72f,
                center = Offset(size.width * 0.86f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowB.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.08f, size.height * 0.42f),
                    radius = size.minDimension * 0.64f
                ),
                radius = size.minDimension * 0.64f,
                center = Offset(size.width * 0.08f, size.height * 0.42f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowA.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.72f, size.height * 0.92f),
                    radius = size.minDimension * 0.55f
                ),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 0.72f, size.height * 0.92f)
            )
        }
        content()
    }
}

@Composable
fun ProtectCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = rememberProtectSurfaceTokens()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (tokens.border != null) {
                    Modifier.border(1.dp, tokens.border, tokens.shape)
                } else {
                    Modifier
                }
            ),
        shape = tokens.shape,
        colors = CardDefaults.cardColors(containerColor = tokens.container),
        elevation = CardDefaults.cardElevation(defaultElevation = tokens.elevation),
        content = content
    )
}

@Composable
fun IconWell(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    background: Color = MaterialTheme.colorScheme.primaryContainer,
    size: Dp = 44.dp
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(size * 0.34f),
        color = background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            IconWell(
                icon = icon,
                size = 34.dp,
                background = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(10.dp))
        }
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = CircleShape
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

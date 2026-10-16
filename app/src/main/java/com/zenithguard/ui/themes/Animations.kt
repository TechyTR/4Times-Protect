package com.zenithguard.ui.theme

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A slow, theme-aware gradient for the main version banner.
 * The emerald palette is used by default; Windows XP keeps its classic blue accent.
 */
@Composable
fun rememberAnimatedOSGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "zenith_banner_gradient")

    val first by transition.animateColor(
        initialValue = scheme.primary.copy(alpha = 0.96f),
        targetValue = scheme.secondary.copy(alpha = 0.88f),
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_first"
    )
    val second by transition.animateColor(
        initialValue = scheme.background,
        targetValue = scheme.primary.copy(alpha = 0.40f),
        animationSpec = infiniteRepeatable(
            animation = tween(6800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_second"
    )
    val third by transition.animateColor(
        initialValue = scheme.secondary.copy(alpha = 0.72f),
        targetValue = scheme.primary.copy(alpha = 0.70f),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_third"
    )

    return Brush.linearGradient(colors = listOf(first, second, third))
}

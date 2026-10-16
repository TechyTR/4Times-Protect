package com.nevruz.fourtimessecured.ui.theme

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush

/**
 * Slow, theme-aware color motion for the version banner.
 * All themes keep a sufficiently saturated surface behind white banner text.
 */
@Composable
fun rememberAnimatedOSGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "zenith_banner_gradient")

    val first by transition.animateColor(
        initialValue = scheme.primary,
        targetValue = scheme.secondary,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_first"
    )
    val second by transition.animateColor(
        initialValue = scheme.primary.copy(alpha = 0.86f),
        targetValue = scheme.secondary.copy(alpha = 0.86f),
        animationSpec = infiniteRepeatable(
            animation = tween(6800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_second"
    )
    val third by transition.animateColor(
        initialValue = scheme.secondary.copy(alpha = 0.92f),
        targetValue = scheme.primary.copy(alpha = 0.92f),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_third"
    )

    return Brush.linearGradient(colors = listOf(first, second, third))
}

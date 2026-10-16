package com.nevruz.fourtimessecured.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nevruz.fourtimessecured.ui.theme.AmbientBackdrop
import com.nevruz.fourtimessecured.ui.theme.LocalZenithThemeMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FourTimesShapeMorphSplash() {
    val transition = rememberInfiniteTransition(label = "shapeMorph")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shapePhase"
    )
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val textColor = MaterialTheme.colorScheme.onBackground

    AmbientBackdrop(themeMode = LocalZenithThemeMode.current) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(144.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.43f
            val path = Path()
            val steps = 180
            for (i in 0..steps) {
                val angle = (i.toFloat() / steps) * (2.0 * PI).toFloat()
                val lobes = 0.10f * cos(4f * angle + phase.value)
                val squareBlend = (0.5f + 0.5f * sin(phase.value * 0.5f)).coerceIn(0f, 1f)
                val cornerShape = 1f - 0.11f * squareBlend * cos(4f * angle)
                val r = radius * (1f + lobes) * cornerShape
                val x = center.x + cos(angle) * r
                val y = center.y + sin(angle) * r
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = primary)
            rotate(degrees = phase.value * 8f, pivot = center) {
                val bolt = Path().apply {
                    moveTo(center.x + size.width * 0.04f, center.y - size.height * 0.25f)
                    lineTo(center.x - size.width * 0.16f, center.y + size.height * 0.02f)
                    lineTo(center.x - size.width * 0.015f, center.y + size.height * 0.02f)
                    lineTo(center.x - size.width * 0.08f, center.y + size.height * 0.25f)
                    lineTo(center.x + size.width * 0.17f, center.y - size.height * 0.07f)
                    lineTo(center.x + size.width * 0.025f, center.y - size.height * 0.07f)
                    close()
                }
                drawPath(bolt, color = onPrimary)
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "4Times Protect",
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.4).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Güvenlik, sade ve net.",
            color = textColor.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    }
}

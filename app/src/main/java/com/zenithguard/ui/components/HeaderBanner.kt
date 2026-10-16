package com.zenithguard.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenithguard.ui.theme.LocalZenithThemeMode
import com.zenithguard.ui.theme.ZenithThemeMode
import com.zenithguard.ui.theme.rememberAnimatedOSGradient

@Composable
fun HeaderBanner() {
    val animatedGradient = rememberAnimatedOSGradient()
    val themeMode = LocalZenithThemeMode.current
    val cornerSize = if (themeMode == ZenithThemeMode.WINDOWS_XP) 6.dp else 28.dp
    val shape = RoundedCornerShape(cornerSize)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(if (themeMode == ZenithThemeMode.WINDOWS_XP) 2.dp else 14.dp, shape)
            .clip(shape)
            .background(animatedGradient)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.36f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                shape = shape
            )
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(if (themeMode == ZenithThemeMode.WINDOWS_XP) 3.dp else 50)
            ) {
                Text(
                    text = "SİSTEM GÜVENLİ VE GÜNCEL",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Zenith Version",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "1",
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 76.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Zenith Guard Core • Build #1",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 12.sp
            )
        }
    }
}

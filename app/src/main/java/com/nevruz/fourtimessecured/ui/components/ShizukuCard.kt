package com.nevruz.fourtimessecured.ui.components

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nevruz.fourtimessecured.core.ShizukuManager
import com.nevruz.fourtimessecured.ui.theme.LocalZenithThemeMode
import com.nevruz.fourtimessecured.ui.theme.StatusGreen
import com.nevruz.fourtimessecured.ui.theme.StatusWarning
import com.nevruz.fourtimessecured.ui.theme.ZenithThemeMode

@Composable
fun ShizukuCard(
    isShizukuActive: Boolean,
    shizukuManager: ShizukuManager,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    val mode = LocalZenithThemeMode.current
    val corner = when (mode) {
        ZenithThemeMode.LIQUID_GLASS -> 26.dp
        ZenithThemeMode.MATERIAL_YOU -> 24.dp
        ZenithThemeMode.AURORA -> 28.dp
        ZenithThemeMode.SYSTEM,
        ZenithThemeMode.LIGHT,
        ZenithThemeMode.DARK -> 20.dp
    }
    val shape = RoundedCornerShape(corner)
    val cardModifier = Modifier
        .fillMaxWidth()
        .animateContentSize()
        .then(
            if (mode == ZenithThemeMode.LIQUID_GLASS || mode == ZenithThemeMode.AURORA) {
                Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
            } else Modifier
        )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (mode == ZenithThemeMode.AURORA || mode == ZenithThemeMode.LIQUID_GLASS) 0.dp else 2.dp
        ),
        modifier = cardModifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isShizukuActive) StatusGreen else StatusWarning)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Shizuku ADB Yetkisi",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = if (isShizukuActive) "Aktif (Derin denetim kullanılabilir)" else "Pasif (Standart mod)",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            if (!isShizukuActive) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Yetki ver",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        shizukuManager.forceStopApp("com.facebook.katana")
                        Toast.makeText(context, "İşlem gönderildi.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        "Derin temizle",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

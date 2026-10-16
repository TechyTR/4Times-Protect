package com.nevruz.fourtimessecured.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nevruz.fourtimessecured.core.ProtectionModule
import com.nevruz.fourtimessecured.ui.theme.LocalZenithThemeMode
import com.nevruz.fourtimessecured.ui.theme.ZenithThemeMode

@Composable
fun ModuleToggleCard(module: ProtectionModule, onToggleChanged: (Boolean) -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = module.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (module.requiresShizuku) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ADB",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = module.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = module.isEnabled,
                onCheckedChange = onToggleChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

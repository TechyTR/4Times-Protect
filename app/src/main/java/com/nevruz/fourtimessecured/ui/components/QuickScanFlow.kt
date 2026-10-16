package com.nevruz.fourtimessecured.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.nevruz.fourtimessecured.security.FileRiskAnalyzer
import com.nevruz.fourtimessecured.security.FileRiskLevel
import com.nevruz.fourtimessecured.security.FileScanner
import com.nevruz.fourtimessecured.security.MalwareHashDatabase
import com.nevruz.fourtimessecured.security.ScanResult
import com.nevruz.fourtimessecured.security.ScanStatus
import com.nevruz.fourtimessecured.security.ScanHistoryStore
import com.nevruz.fourtimessecured.security.SelectedDocumentDeleteManager
import com.nevruz.fourtimessecured.security.DeleteDocumentResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuickScanFlow(
    enabled: Boolean,
    saveHistory: Boolean = true,
    onStatusChanged: (String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ScanResult?>(null) }
    var resultFindings by remember { mutableStateOf(emptyList<com.nevruz.fourtimessecured.security.FileRiskFinding>()) }
    var message by remember { mutableStateOf<String?>(null) }
    var scanJob by remember { mutableStateOf<Job?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && enabled) {
            selectedUri = uri
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            scanning = true
            showResult = false
            message = null
            onStatusChanged("SCANNING", "Dosya analiz ediliyor")
            scanJob = scope.launch {
                try {
                    val scan = withContext(Dispatchers.IO) {
                        FileScanner.scanSelectedDocument(context, uri, MalwareHashDatabase.snapshot())
                    }
                    result = scan
                    if (saveHistory) ScanHistoryStore(context).add(scan)
                    resultFindings = FileRiskAnalyzer.analyze(scan.name, scan.sha256)
                    val knownBad = scan.status == ScanStatus.KNOWN_MALWARE ||
                        resultFindings.any { it.level == FileRiskLevel.HIGH }
                    val suspicious = resultFindings.any { it.level == FileRiskLevel.REVIEW }
                    when {
                        scan.status == ScanStatus.ERROR -> onStatusChanged("RISK", "Tarama tamamlanamadı")
                        knownBad -> onStatusChanged("DANGER", "Zararlı imza eşleşmesi bulundu")
                        suspicious -> onStatusChanged("REVIEW", "Şüpheli dosya göstergesi bulundu")
                        else -> onStatusChanged("SCANNED", "Bilinen zararlı imza eşleşmesi bulunmadı")
                    }
                    showResult = true
                } catch (_: CancellationException) {
                    onStatusChanged("RISK", "Tarama iptal edildi")
                } catch (e: Exception) {
                    message = e.localizedMessage ?: "Dosya taranamadı."
                    onStatusChanged("RISK", "Tarama tamamlanamadı")
                    showResult = true
                } finally {
                    scanning = false
                }
            }
        }
    }

    val scanPulse = rememberInfiniteTransition(label = "scan-button-pulse")
    val scanScale by scanPulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan-button-scale"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Hızlı tarama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (enabled) "Bir dosya seç, imza ve risk göstergelerini kontrol edelim." else "Dosya taraması ayarlardan kapalı.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        val scale = if (enabled && !scanning) scanScale else 1f
                        scaleX = scale
                        scaleY = scale
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                onClick = { if (enabled && !scanning) picker.launch(arrayOf("*/*")) },
                enabled = enabled && !scanning,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val scanIconColor = MaterialTheme.colorScheme.onPrimary
                    Canvas(modifier = Modifier.size(34.dp)) {
                        drawShieldScanIcon(
                            color = scanIconColor,
                            strokeWidth = 2.6.dp.toPx()
                        )
                    }
                }
            }
        }
        Button(
            onClick = { if (enabled && !scanning) picker.launch(arrayOf("*/*")) },
            enabled = enabled && !scanning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (scanning) "Taranıyor…" else "Dosya seç ve tara", fontWeight = FontWeight.SemiBold)
        }
        if (!enabled) {
            Text("Bu özellik Ayarlar → Güvenlik bölümünden açılabilir.", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (scanning) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Dosya taranıyor", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Dosya okunuyor ve güvenlik göstergeleri inceleniyor.")
                    Text("Bu ekran yalnızca seçtiğin dosyayı tarar; Android'in korumalı uygulama verilerine erişemez.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scanJob?.cancel()
                    scanning = false
                    onStatusChanged("RISK", "Tarama iptal edildi")
                }) { Text("Vazgeç") }
            }
        )
    }

    if (showResult) {
        val scan = result
        val isMalware = scan?.status == ScanStatus.KNOWN_MALWARE ||
            resultFindings.any { it.level == FileRiskLevel.HIGH }
        val isError = scan?.status == ScanStatus.ERROR || message != null
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = {
                Text(
                    when {
                        isError -> "Tarama tamamlanamadı"
                        isMalware -> "Tehlike uyarısı"
                        resultFindings.any { it.level == FileRiskLevel.REVIEW } -> "Şüpheli göstergeler"
                        else -> "Tarama tamamlandı"
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (isMalware || isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(scan?.name ?: "Dosya")
                    Text(message ?: scan?.reason.orEmpty())
                    resultFindings.forEach { finding ->
                        Text("${finding.title}: ${finding.explanation}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (!isMalware && !isError) {
                        Text("İmza listesi sınırlı olduğundan bu sonuç dosyanın kesin güvenli olduğunu garanti etmez.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                if (isMalware && selectedUri != null) {
                    TextButton(onClick = {
                        val uri = selectedUri
                        showResult = false
                        if (uri != null) {
                            scope.launch {
                                val deleteResult = withContext(Dispatchers.IO) {
                                    SelectedDocumentDeleteManager.delete(
                                        resolver = context.contentResolver,
                                        uri = uri,
                                        userConfirmed = true
                                    )
                                }
                                message = when (deleteResult) {
                                    DeleteDocumentResult.Deleted -> "Dosya silindi."
                                    DeleteDocumentResult.ConfirmationRequired -> "Silme onayı gerekiyor."
                                    is DeleteDocumentResult.Failed -> "Silinemedi: ${deleteResult.reason}"
                                }
                            }
                        }
                    }) { Text("Sil") }
                } else {
                    TextButton(onClick = { showResult = false }) { Text("Tamam") }
                }
            },
            dismissButton = {
                if (isMalware) {
                    TextButton(onClick = { showResult = false }) { Text("Vazgeç") }
                }
            }
        )
    }

    if (message != null && !showResult && !scanning) {
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("Bilgi") },
            text = { Text(message.orEmpty()) },
            confirmButton = { TextButton(onClick = { message = null }) { Text("Tamam") } }
        )
    }
}


private fun DrawScope.drawShieldScanIcon(color: androidx.compose.ui.graphics.Color, strokeWidth: Float) {
    val shield = Path().apply {
        moveTo(size.width * 0.5f, size.height * 0.07f)
        lineTo(size.width * 0.82f, size.height * 0.20f)
        lineTo(size.width * 0.78f, size.height * 0.57f)
        cubicTo(size.width * 0.76f, size.height * 0.78f, size.width * 0.64f, size.height * 0.94f, size.width * 0.5f, size.height * 0.94f)
        cubicTo(size.width * 0.36f, size.height * 0.94f, size.width * 0.24f, size.height * 0.78f, size.width * 0.22f, size.height * 0.57f)
        lineTo(size.width * 0.18f, size.height * 0.20f)
        close()
    }
    drawPath(shield, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    drawLine(
        color = color,
        start = Offset(size.width * 0.34f, size.height * 0.51f),
        end = Offset(size.width * 0.46f, size.height * 0.63f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.46f, size.height * 0.63f),
        end = Offset(size.width * 0.68f, size.height * 0.38f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

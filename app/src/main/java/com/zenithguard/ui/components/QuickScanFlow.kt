package com.zenithguard.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.zenithguard.security.FileRiskAnalyzer
import com.zenithguard.security.FileRiskLevel
import com.zenithguard.security.FileScanner
import com.zenithguard.security.MalwareHashDatabase
import com.zenithguard.security.ScanResult
import com.zenithguard.security.ScanStatus
import com.zenithguard.security.SelectedDocumentDeleteManager
import com.zenithguard.security.DeleteDocumentResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuickScanFlow(
    enabled: Boolean,
    onStatusChanged: (String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ScanResult?>(null) }
    var resultFindings by remember { mutableStateOf(emptyList<com.zenithguard.security.FileRiskFinding>()) }
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Hemen tara", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (enabled) "Bir dosyayı seçip güvenlik kontrolünü başlat." else "Dosya taraması ayarlardan kapalı.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = { if (enabled && !scanning) picker.launch(arrayOf("*/*")) },
                enabled = enabled && !scanning,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⌁", fontSize = 32.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        Button(
            onClick = { if (enabled && !scanning) picker.launch(arrayOf("*/*")) },
            enabled = enabled && !scanning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dosya seç ve tara")
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

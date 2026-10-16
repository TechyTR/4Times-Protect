
package com.zenithguard.ui.components

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.zenithguard.security.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun FileScanCard() {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("") }
    var workId by remember { mutableStateOf<UUID?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf("") }
    var scanReason by remember { mutableStateOf("") }
    var scanHash by remember { mutableStateOf("") }
    var findings by remember { mutableStateOf<List<FileRiskFinding>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteMessage by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedName = uri.lastPathSegment ?: "Seçilen dosya"
            error = null
            deleteMessage = null
            scanStatus = ""
            scanReason = ""
            scanHash = ""
            findings = emptyList()

            // Belge sağlayıcısı izin veriyorsa erişimi arka plan görevi için sakla.
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Bazı sağlayıcılar kalıcı izin vermeyebilir.
            }

            val request = OneTimeWorkRequestBuilder<DocumentScanWorker>()
                .setInputData(
                    workDataOf(DocumentScanWorker.KEY_URI to uri.toString())
                )
                .build()

            workId = request.id
            scanning = true

            workManager.enqueueUniqueWork(
                "zenith-scan-${request.id}",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    LaunchedEffect(workId) {
        val id = workId ?: return@LaunchedEffect

        while (true) {
            val info = try {
                withContext(Dispatchers.IO) {
                    workManager.getWorkInfoById(id).get()
                }
            } catch (e: Exception) {
                error = "Tarama durumu okunamadı: ${e.localizedMessage ?: "bilinmeyen hata"}"
                scanning = false
                break
            }

            if (info != null && info.state.isFinished) {
                scanning = false

                val output = info.outputData
                scanStatus = output.getString(DocumentScanWorker.KEY_STATUS).orEmpty()
                scanReason = output.getString(DocumentScanWorker.KEY_REASON).orEmpty()
                scanHash = output.getString(DocumentScanWorker.KEY_SHA256).orEmpty()
                selectedName = output.getString(DocumentScanWorker.KEY_NAME)
                    ?: selectedName

                findings = FileRiskAnalyzer.analyze(selectedName, scanHash)

                if (info.state != WorkInfo.State.SUCCEEDED && scanReason.isBlank()) {
                    error = "Tarama tamamlanamadı. Dosya erişimini ve izinleri kontrol et."
                }
                break
            }

            delay(400)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Dosya Güvenlik Taraması", style = MaterialTheme.typography.titleMedium)

            Text(
                "Android dosya seçicisinden bir dosya seç. Zenith Guard dosyanın SHA-256 özetini hesaplar ve mevcut imza listesiyle karşılaştırır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { filePicker.launch(arrayOf("*/*")) },
                enabled = !scanning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dosya seç ve tara")
            }

            if (scanning) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator()
                    Text("Dosya arka planda taranıyor…")
                }
            }

            if (selectedName.isNotBlank()) {
                Text("Dosya: $selectedName", style = MaterialTheme.typography.bodyMedium)
            }

            if (scanStatus.isNotBlank()) {
                val statusText = when (scanStatus) {
                    ScanStatus.KNOWN_MALWARE.name -> "Bilinen zararlı imza eşleşmesi"
                    ScanStatus.SUSPICIOUS.name -> "Şüpheli"
                    ScanStatus.CLEAN_UNVERIFIED.name -> "Bilinen imza eşleşmesi bulunmadı"
                    ScanStatus.ERROR.name -> "Tarama hatası"
                    else -> scanStatus
                }

                Text(
                    statusText,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (scanStatus == ScanStatus.KNOWN_MALWARE.name ||
                        scanStatus == ScanStatus.ERROR.name
                    ) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            if (scanReason.isNotBlank()) {
                Text(scanReason, style = MaterialTheme.typography.bodySmall)
            }

            if (scanHash.isNotBlank()) {
                Text("SHA-256", style = MaterialTheme.typography.labelLarge)
                Text(scanHash, style = MaterialTheme.typography.bodySmall)
            }

            findings.forEach { finding ->
                Text(
                    "${finding.level}: ${finding.title}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    finding.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (error != null) {
                Text(
                    error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (deleteMessage != null) {
                Text(deleteMessage.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }

            if (selectedUri != null && !scanning && scanStatus.isNotBlank()) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seçilen dosyayı silmeyi dene…")
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            fileName = selectedName,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                val uri = selectedUri ?: return@DeleteConfirmationDialog

                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        SelectedDocumentDeleteManager.delete(
                            resolver = context.contentResolver,
                            uri = uri,
                            userConfirmed = true
                        )
                    }

                    deleteMessage = when (result) {
                        DeleteDocumentResult.Deleted ->
                            "Dosya silindi. Dosyanın erişilebilirliğini yeniden kontrol et."
                        DeleteDocumentResult.ConfirmationRequired ->
                            "Silme işlemi için onay gerekiyor."
                        is DeleteDocumentResult.Failed ->
                            "Silinemedi: ${result.reason}"
                    }
                }
            }
        )
    }
}

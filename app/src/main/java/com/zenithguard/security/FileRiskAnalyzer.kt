package com.zenithguard.security

import java.io.File
import java.util.Locale

enum class FileRiskLevel { INFO, REVIEW, HIGH }

data class FileRiskFinding(
    val level: FileRiskLevel,
    val title: String,
    val explanation: String
)

/**
 * Heuristic triage only. File names/extensions are not proof of malware.
 * This class does not execute or modify the inspected file.
 */
object FileRiskAnalyzer {
    private val executableLikeExtensions = setOf(
        "apk", "dex", "jar", "so", "sh", "elf", "exe", "dll", "bat", "cmd", "ps1"
    )

    fun analyze(fileName: String, sha256: String = ""): List<FileRiskFinding> {
        val findings = mutableListOf<FileRiskFinding>()
        val lower = fileName.lowercase(Locale.ROOT)
        val parts = lower.split('.').filter { it.isNotBlank() }

        if (parts.size >= 3) {
            val penultimate = parts[parts.lastIndex - 1]
            val last = parts.last()
            if (penultimate in setOf("pdf", "jpg", "jpeg", "png", "mp3", "mp4", "doc", "docx") &&
                last in executableLikeExtensions
            ) {
                findings += FileRiskFinding(
                    FileRiskLevel.REVIEW,
                    "Çift uzantılı dosya",
                    "Dosya adı belge veya medya dosyası gibi görünüp çalıştırılabilir bir uzantıyla bitiyor. İçeriğini doğrulayın."
                )
            }
        }

        if (parts.lastOrNull() in executableLikeExtensions) {
            findings += FileRiskFinding(
                FileRiskLevel.INFO,
                "Çalıştırılabilir dosya türü",
                "Bu tür dosyalar meşru da olabilir, zararlı da. Kaynağını ve imzasını kontrol edin."
            )
        }

        if (sha256.isNotBlank() && MalwareHashDatabase.contains(sha256)) {
            findings += FileRiskFinding(
                FileRiskLevel.HIGH,
                "Bilinen zararlı imzası",
                "Dosyanın SHA-256 özeti yerel imza listesiyle eşleşti."
            )
        }

        if (findings.isEmpty()) {
            findings += FileRiskFinding(
                FileRiskLevel.INFO,
                "Belirgin isim tabanlı uyarı yok",
                "Bu sonuç dosyanın güvenli olduğunu kanıtlamaz; içerik tabanlı analiz ve güncel imzalar gerekir."
            )
        }

        return findings
    }

    fun analyze(file: File, sha256: String = ""): List<FileRiskFinding> =
        analyze(file.name, sha256)
}

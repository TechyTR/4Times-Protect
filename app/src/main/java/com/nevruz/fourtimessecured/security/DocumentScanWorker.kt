package com.nevruz.fourtimessecured.security

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data

/** Background scan of a document URI explicitly chosen by the user. */
class DocumentScanWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val rawUri = inputData.getString(KEY_URI) ?: return Result.failure()
        val uri = Uri.parse(rawUri)
        return try {
            val scan = FileScanner.scanSelectedDocument(applicationContext, uri, MalwareHashDatabase.snapshot())
            ScanHistoryStore(applicationContext).add(scan)
            val output = Data.Builder()
                .putString(KEY_NAME, scan.name)
                .putString(KEY_STATUS, scan.status.name)
                .putString(KEY_SHA256, scan.sha256)
                .putString(KEY_REASON, scan.reason.orEmpty())
                .putLong(KEY_SIZE, scan.sizeBytes)
                .build()
            if (scan.status == ScanStatus.ERROR) Result.failure(output) else Result.success(output)
        } catch (_: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val KEY_URI = "document_uri"
        const val KEY_NAME = "result_name"
        const val KEY_STATUS = "result_status"
        const val KEY_SHA256 = "result_sha256"
        const val KEY_REASON = "result_reason"
        const val KEY_SIZE = "result_size_bytes"
    }
}

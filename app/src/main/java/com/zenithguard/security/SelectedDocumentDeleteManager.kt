package com.zenithguard.security

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract

sealed class DeleteDocumentResult {
    data object Deleted : DeleteDocumentResult()
    data object ConfirmationRequired : DeleteDocumentResult()
    data class Failed(val reason: String) : DeleteDocumentResult()
}

/**
 * Only deletes the exact document URI selected by the user through Android's
 * document picker. Never call with userConfirmed=true without showing a
 * confirmation dialog that identifies the file.
 */
object SelectedDocumentDeleteManager {
    fun delete(
        resolver: ContentResolver,
        uri: Uri,
        userConfirmed: Boolean
    ): DeleteDocumentResult {
        if (!userConfirmed) return DeleteDocumentResult.ConfirmationRequired

        return try {
            if (DocumentsContract.deleteDocument(resolver, uri)) {
                DeleteDocumentResult.Deleted
            } else {
                DeleteDocumentResult.Failed("Android belgeyi silemedi veya izin vermedi.")
            }
        } catch (e: SecurityException) {
            DeleteDocumentResult.Failed("Bu dosyayı silmek için yetki yok.")
        } catch (e: Exception) {
            DeleteDocumentResult.Failed(e.message ?: "Silme işlemi başarısız.")
        }
    }
}

package com.example.settlementapp.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.ln
import kotlin.math.pow

object ReceiptFiles {

    private fun receiptDir(context: Context): File =
        File(context.getExternalFilesDir(null), "receipts").apply { mkdirs() }

    /** 새 영수증 사진을 저장할 파일과 FileProvider Uri 를 생성 */
    fun newReceiptUri(context: Context): Uri {
        val file = File(receiptDir(context), "receipt_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return if (digitGroups == 0) {
            "${bytes} B"
        } else {
            String.format("%.1f %s", value, units[digitGroups])
        }
    }

    fun fileSizeLabel(context: Context, uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        val uri = Uri.parse(uriString)
        val bytes = context.contentResolver.openFileDescriptor(uri, "r")?.use {
            it.statSize
        } ?: uri.path?.let { File(it).takeIf(File::exists)?.length() }
        return bytes?.let { formatFileSize(it) }
    }

    fun deleteReceiptFile(context: Context, uriString: String?) {
        if (uriString.isNullOrBlank()) return
        val uri = Uri.parse(uriString)
        when (uri.scheme) {
            "file" -> uri.path?.let { File(it).delete() }
            "content" -> runCatching {
                context.contentResolver.delete(uri, null, null)
            }
            else -> {
                uri.path?.let { File(it).delete() }
            }
        }
    }
}

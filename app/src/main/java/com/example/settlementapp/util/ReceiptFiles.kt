package com.example.settlementapp.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

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
}

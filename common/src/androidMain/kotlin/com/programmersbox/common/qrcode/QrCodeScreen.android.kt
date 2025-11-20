package com.programmersbox.common.qrcode

import android.content.Context
import android.content.Intent
import android.content.Intent.createChooser
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.net.toUri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await

actual class QrCodeRepository(
    private val context: Context,
) {
    val scanner = BarcodeScanning.getClient()
    actual suspend fun getInfoFromQRCode(
        bitmap: ImageBitmap,
    ): Result<List<String>> = runCatching { InputImage.fromBitmap(bitmap.asAndroidBitmap(), 0) }
        .mapCatching { scanner.process(it).await() }
        .mapCatching { barcodes -> barcodes.mapNotNull { it.displayValue } }

    actual suspend fun shareUrl(url: String, title: String) {
        runCatching {
            context.startActivity(
                createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, url)
                        putExtra(Intent.EXTRA_TITLE, title)
                    },
                    "Share"
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }
    }

    actual suspend fun shareImage(
        bitmap: ImageBitmap,
        title: String,
    ) {
        runCatching { bitmap.asAndroidBitmap().saveToDisk(title) }
            .onSuccess { shareBitmap(context, it, title) }
    }

    actual suspend fun saveImage(bitmap: ImageBitmap, title: String) {
        runCatching { bitmap.asAndroidBitmap().saveToDisk(title) }
            .onSuccess { Toast.makeText(context, "Qr Code Saved!", Toast.LENGTH_LONG).show() }
    }

    //Copied from https://github.com/android/snippets/blob/latest/compose/snippets/src/main/java/com/example/compose/snippets/graphics/AdvancedGraphicsSnippets.kt#L123
    private suspend fun Bitmap.saveToDisk(title: String): Uri {
        return MediaStore.Images.Media.insertImage(
            context.contentResolver,
            this,
            title,
            "QR Code"
        ).toUri()
    }

    private fun shareBitmap(context: Context, uri: Uri, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            createChooser(intent, "Share your image")
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
            null
        )
    }
}

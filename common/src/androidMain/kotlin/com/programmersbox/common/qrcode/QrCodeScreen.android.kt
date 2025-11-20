package com.programmersbox.common.qrcode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.createChooser
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await
import java.io.File

actual class QrCodeRepository(
    private val context: Context,
) {
    private val clipboardManager by lazy {
        context.getSystemService<ClipboardManager>()
    }
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
        // Save into the app's internal storage (not visible to gallery apps) then share
        runCatching { bitmap.asAndroidBitmap().saveToInternal(title) }
            .onSuccess { shareBitmap(context, it, title) }
    }

    actual suspend fun saveImage(bitmap: ImageBitmap, title: String) {
        runCatching { bitmap.asAndroidBitmap().saveToDisk(title) }
            .onSuccess { Toast.makeText(context, "Qr Code Saved!", Toast.LENGTH_LONG).show() }
    }

    private suspend fun Bitmap.saveToDisk(title: String): Uri {
        return MediaStore.Images.Media.insertImage(
            context.contentResolver,
            this,
            title,
            "QR Code"
        ).toUri()
    }

    // Save bitmap into app-internal storage and return a FileProvider content URI suitable for sharing
    private suspend fun Bitmap.saveToInternal(title: String): Uri {
        val file = File(context.filesDir, "$title-${System.currentTimeMillis()}.png")
        if (!file.exists()) file.createNewFile()
        file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

        // Use FileProvider so external apps can read the file via a content:// URI without exposing the path
        return FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".fileprovider",
            file
        )
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun shareBitmap(context: Context, uri: Uri, title: String) {
        clipboardManager?.setPrimaryClip(ClipData.newUri(context.contentResolver, title, uri))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            Toast.makeText(context, "Qr Code Copied!", Toast.LENGTH_LONG).show()
    }
}

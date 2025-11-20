package com.programmersbox.common.qrcode

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import ca.gosyer.appdirs.AppDirs
import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException


actual class QrCodeRepository(
    private val appDirs: AppDirs,
) {
    private val reader: QRCodeReader by lazy { QRCodeReader() }

    actual suspend fun getInfoFromQRCode(
        bitmap: ImageBitmap,
    ): Result<List<String>> = runCatching {

        // 1. Load the image
        val image: BufferedImage = bitmap.toAwtImage()

        // 3. Read the barcode
        val result = reader.decode(
            BinaryBitmap(
                HybridBinarizer(BufferedImageLuminanceSource(image))
            )
        )

        // 4. Return the decoded text
        listOf(result.text)
    }

    actual suspend fun shareImage(bitmap: ImageBitmap, title: String) {
        val transferable = ImageTransferable(bitmap.toAwtImage())
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(transferable, null)
    }

    actual suspend fun saveImage(bitmap: ImageBitmap, title: String) {
        appDirs.getUserDataDir()
        val file = File(appDirs.getUserDataDir(), "$title-${System.currentTimeMillis()}.png")
        if (!file.exists()) file.createNewFile()
        file.writeBytes(bitmap.encodeToByteArray())
        println("Saved to ${file.absolutePath}")
    }

    actual suspend fun shareUrl(url: String, title: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(url), null)
    }

    private class ImageTransferable(private val image: Image) : Transferable {

        override fun getTransferDataFlavors(): Array<DataFlavor?> {
            return arrayOf<DataFlavor?>(DataFlavor.imageFlavor)
        }

        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
            return DataFlavor.imageFlavor.equals(flavor)
        }

        @Throws(UnsupportedFlavorException::class, IOException::class)
        override fun getTransferData(flavor: DataFlavor?): Any {
            if (DataFlavor.imageFlavor.equals(flavor)) {
                return image
            }
            throw UnsupportedFlavorException(flavor)
        }
    }
}
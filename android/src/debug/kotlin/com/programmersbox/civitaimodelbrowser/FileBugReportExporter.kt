package com.programmersbox.civitaimodelbrowser

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.ms.square.debugoverlay.BugReportExporter
import com.ms.square.debugoverlay.core.R
import com.ms.square.debugoverlay.formatBugReportMarkdown
import com.ms.square.debugoverlay.model.BugReport
import com.ms.square.debugoverlay.model.ExportResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Default exporter that shares the bug report via Android's share sheet.
 * Uses Intent.ACTION_SEND with FileProvider for secure file sharing.
 */
internal object FileBugReportExporter : BugReportExporter {

    override suspend fun export(context: Context, report: BugReport): ExportResult {
        println(report)
        println(report.summary)
        println(report.archive)

        val file = report.archive

        val f = File(
            context.cacheDir,
            "bugreport_${System.currentTimeMillis()}.zip"
        )

        withContext(Dispatchers.IO) {
            // 1. Open the PFD in a 'use' block so it ALWAYS closes correctly
            context.contentResolver.openFileDescriptor(f.toUri(), "wt")?.use { pfd ->
                // 2. Create the streams
                ZipOutputStream(FileOutputStream(pfd.fileDescriptor)).use { zip ->
                    zip.putNextEntry(ZipEntry(file.fileName))
                    file.openInputStream().use {
                        zip.write(it.readBytes())
                    }
                    // 3. Ensure zip finishes writing its footer/central directory
                    zip.finish()
                }
            }
        }

        val authority = context.applicationContext.packageName + ".fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, f)

        val subject =
            context.getString(R.string.debugoverlay_bug_report_subject, f.nameWithoutExtension)
        val chooserTitle = context.getString(R.string.debugoverlay_share_bug_report)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            clipData = ClipData.newRawUri(null, uri)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, formatBugReportMarkdown(report.summary))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return runCatchingNonCancellation {
            // Switch to Main for UI operation (startActivity)
            withContext(Dispatchers.Main) {
                context.startActivity(
                    Intent.createChooser(intent, chooserTitle).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
            ExportResult.Initiated
        }.getOrElse { e ->
            ExportResult.Failure(e)
        }
    }
}

@Suppress("TooGenericExceptionCaught")
internal inline fun <T> runCatchingNonCancellation(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
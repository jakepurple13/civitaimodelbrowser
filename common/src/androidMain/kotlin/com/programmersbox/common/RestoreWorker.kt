package com.programmersbox.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import com.programmersbox.common.backup.BackupRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.measureTime

class RestoreWorker(
    appContext: Context,
    params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val toasterState: ToasterState,
    private val applicationIcon: ApplicationIcon
) : CoroutineWorker(
    appContext,
    params
) {
    override suspend fun doWork(): Result {
        val file = inputData.getString("file") ?: return Result.failure()
        val includeFavorites = inputData.getBoolean("includeFavorites", false)
        val includeBlacklisted = inputData.getBoolean("includeBlacklisted", false)
        val includeSettings = inputData.getBoolean("includeSettings", false)
        val includeSearchHistory = inputData.getBoolean("includeSearchHistory", false)
        val listsToInclude = inputData.getStringArray("listsToInclude")?.toList() ?: emptyList()

        println("Will restore $includeFavorites $includeBlacklisted $includeSettings $includeSearchHistory")
        println("Will restore lists: ${listsToInclude.size}")

        println("Restoring $file")
        val platformFile = PlatformFile(file)
        val readItems = backupRepository.readItems(platformFile)
        val duration = measureTime {
            backupRepository.restoreItems(
                backupItems = readItems.copy(lists = readItems.lists?.filter { it.item.uuid in listsToInclude }),
                includeSettings = includeSettings,
                includeFavorites = includeFavorites,
                includeBlacklisted = includeBlacklisted,
                includeSearchHistory = includeSearchHistory,
            )
        }
        println("Restored in $duration")

        applicationContext.getSystemService<NotificationManager>()?.let { manager ->
            manager.createNotificationChannel(
                NotificationChannel(
                    "restore_channel",
                    "Restore Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            manager.notify(
                1,
                NotificationCompat.Builder(applicationContext, "restore_channel")
                    .setContentTitle("Restore Complete")
                    .setContentText("Restore Complete in $duration")
                    .setSmallIcon(applicationIcon.icon)
                    .build()
            )
        }

        toasterState.show(
            "Backup Complete in $duration",
            type = ToastType.Success
        )
        return Result.success()
    }
}
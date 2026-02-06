package com.programmersbox.civitaimodelbrowser.com.programmersbox.civitaimodelbrowser

import android.content.Context
import com.ms.square.debugoverlay.BugReportExporter
import com.ms.square.debugoverlay.DebugOverlay
import com.ms.square.debugoverlay.model.BugReport
import com.ms.square.debugoverlay.model.ExportResult
import com.programmersbox.civitaimodelbrowser.CivitApplication

class DebugCivitApplication : CivitApplication() {
    override fun onCreate() {
        super.onCreate()

        DebugOverlay.configure {
            bugReportExporter = object : BugReportExporter {
                override suspend fun export(
                    context: Context,
                    report: BugReport
                ): ExportResult {
                    return ExportResult.Success
                }
            }

            /*networkRequestSource = object : NetworkRequestSource {
                override val requests: Flow<List<NetworkRequest>>
                    get() = TODO("Not yet implemented")

            }*/
        }
    }
}
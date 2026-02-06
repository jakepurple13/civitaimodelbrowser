package com.programmersbox.civitaimodelbrowser

import com.ms.square.debugoverlay.DebugOverlay

class DebugCivitApplication : CivitApplication() {
    override fun onCreate() {
        super.onCreate()

        DebugOverlay.configure {
            /*bugReportExporter = object : BugReportExporter {
                override suspend fun export(
                    context: Context,
                    report: BugReport
                ): ExportResult {
                    return ExportResult.Success
                }
            }*/

            /*networkRequestSource = object : NetworkRequestSource {
                override val requests: Flow<List<NetworkRequest>>
                    get() = TODO("Not yet implemented")

            }*/
        }
    }
}
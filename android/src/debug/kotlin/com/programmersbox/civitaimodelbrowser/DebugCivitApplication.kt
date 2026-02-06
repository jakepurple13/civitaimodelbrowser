package com.programmersbox.civitaimodelbrowser

import com.ms.square.debugoverlay.DebugOverlay
import org.koin.core.module.Module

class DebugCivitApplication : CivitApplication() {
    override fun Module.includeMore() {
        includes(debugModule)
    }

    override fun onCreate() {
        super.onCreate()
        DebugOverlay.configure {
            bugReportExporter = FileBugReportExporter
        }
    }
}
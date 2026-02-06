package com.programmersbox.civitaimodelbrowser

import org.koin.core.module.Module

class DebugCivitApplication : CivitApplication() {
    override fun Module.includeMore() {
        includes(debugModule)
    }
}
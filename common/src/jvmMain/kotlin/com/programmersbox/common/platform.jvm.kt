package com.programmersbox.common

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual class DownloadHandler {
    actual suspend fun download(url: String, name: String) {
    }
}

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
}
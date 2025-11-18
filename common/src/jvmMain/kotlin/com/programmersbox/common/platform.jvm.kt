package com.programmersbox.common

import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual class DownloadHandler(
    private val network: Network
) {
    actual suspend fun download(url: String, name: String) {
        val file = File(
            System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "CivitAi",
            name
        )

        if (!file.exists()) {
            file.mkdirs()
            file.createNewFile()
        }
        file.writeBytes(network.client.get(url).body())
    }
}

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
}
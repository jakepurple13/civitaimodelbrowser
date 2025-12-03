package com.programmersbox.civitaimodelbrowser

import android.app.Application
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.cmpModules
import com.programmersbox.common.createPlatformModule
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.qrcode.QrCodeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class CivitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CivitApplication)
            modules(
                module {
                    single { getDatabaseBuilder(get()) }
                    single<() -> String> { { filesDir.resolve("androidx.preferences_pb").absolutePath } }
                    factory { ApplicationInfo(BuildConfig.VERSION_NAME) }
                    singleOf(::QrCodeRepository)
                },
                cmpModules(),
                createPlatformModule()
            )
        }
    }
}
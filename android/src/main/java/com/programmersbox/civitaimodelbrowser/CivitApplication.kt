package com.programmersbox.civitaimodelbrowser

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.RestoreWorker
import com.programmersbox.common.backup.ApplicationIcon
import com.programmersbox.common.backup.Zipper
import com.programmersbox.common.createPlatformModule
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.qrcode.QrCodeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class CivitApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CivitApplication)
            modules(
                module {
                    single { getDatabaseBuilder(get()) }
                    single<() -> String> { { filesDir.resolve("androidx.preferences_pb").absolutePath } }
                    factory { ApplicationInfo(BuildConfig.VERSION_NAME) }
                    singleOf(::Zipper)
                    singleOf(::QrCodeRepository)
                    workerOf(::RestoreWorker)
                    factory { ApplicationIcon(R.drawable.civitai_logo) }
                },
                cmpModules(),
                createPlatformModule()
            )
            workManagerFactory()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerInitializationExceptionHandler { it.throwable.printStackTrace() }
            .setSchedulingExceptionHandler { it.printStackTrace() }
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.VERBOSE else Log.ERROR)
            .build()
}
package com.programmersbox.civitaimodelbrowser

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.RestoreWorker
import com.programmersbox.common.createPlatformModule
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.presentation.backup.ApplicationIcon
import com.programmersbox.common.presentation.backup.Zipper
import com.programmersbox.common.presentation.qrcode.QrCodeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

open class CivitApplication : Application(), Configuration.Provider {

    open fun Module.includeMore() {}

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
                    includeMore()
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
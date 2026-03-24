package com.programmersbox.civitaimodelbrowser

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.presentation.backup.ApplicationIcon
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

open class CivitApplication : Application(), Configuration.Provider {

    open fun Module.includeMore() {}

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CivitApplication)
            modules(
                module {
                    single<() -> String> { { filesDir.resolve("androidx.preferences_pb").absolutePath } }
                    factory { ApplicationIcon(R.drawable.civitai_logo) }
                    includeMore()
                },
                cmpModules(),
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
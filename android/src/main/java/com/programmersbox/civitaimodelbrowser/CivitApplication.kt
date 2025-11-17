package com.programmersbox.civitaimodelbrowser

import android.app.Application
import com.programmersbox.common.cmpModules
import com.programmersbox.common.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
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
                },
                cmpModules()
            )
        }
    }
}
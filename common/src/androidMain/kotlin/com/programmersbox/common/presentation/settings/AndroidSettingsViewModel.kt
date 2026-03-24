package com.programmersbox.common.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import kotlin.time.measureTime

class AndroidSettingsViewModel(
    private val snackBarState: ToasterState
) : ViewModel() {

    fun clearCache(context: Context) {
        runCatching {
            measureTime {
                context
                    .cacheDir
                    .listFiles()
                    ?.forEach {
                        if (it.name == "journal" || it.name.startsWith("journal.")) {
                            println("Skipping ${it.name}")
                            return@forEach
                        }
                        println("Deleting ${it.name}")
                        if (it.deleteRecursively()) {
                            println("Deleted ${it.name}")
                        } else {
                            println("Failed to delete ${it.name}")
                        }
                    }
            }
        }
            .onSuccess {
                snackBarState.show(
                    "Cache Cleared in $it",
                    type = ToastType.Success
                )
            }
            .onFailure {
                snackBarState.show(
                    "Failed to Clear Cache",
                    type = ToastType.Error
                )
            }
            .onFailure { it.printStackTrace() }
    }

}
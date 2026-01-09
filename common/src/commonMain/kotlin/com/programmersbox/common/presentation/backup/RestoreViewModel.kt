package com.programmersbox.common.presentation.backup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.analyticsEvent
import com.programmersbox.common.di.NavigationHandler
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RestoreViewModel(
    private val backupRepository: BackupRepository,
    private val toasterState: ToasterState,
    private val navigationHandler: NavigationHandler,
) : ViewModel() {
    var backupItems by mutableStateOf<BackupItems?>(null)

    var includeFavorites by mutableStateOf(true)
    var includeBlacklisted by mutableStateOf(true)
    var includeSettings by mutableStateOf(true)
    var includeSearchHistory by mutableStateOf(true)
    val listsToInclude = mutableStateListOf<String>()
    private var platformFile: PlatformFile? = null

    var uiState by mutableStateOf(
        RestoreUiState(
            isReading = false,
            isRestoring = false,
            error = null
        )
    )

    fun read(platformFile: PlatformFile) {
        viewModelScope.launch {
            uiState = uiState.copy(isReading = true)
            this@RestoreViewModel.platformFile = platformFile
            runCatching { backupItems = backupRepository.readItems(platformFile) }
                .onFailure {
                    uiState = uiState.copy(error = it)
                    it.printStackTrace()
                }
            listsToInclude.clear()
            backupItems?.lists?.forEach { listsToInclude.add(it.item.uuid) }
            uiState = uiState.copy(isReading = false)
        }
    }

    fun restore() {
        analyticsEvent(
            "restore",
            mapOf(
                "includeFavorites" to includeFavorites,
                "includeBlacklisted" to includeBlacklisted,
                "includeSettings" to includeSettings,
                "includeSearchHistory" to includeSearchHistory,
                "listsToInclude" to listsToInclude.size
            )
        )
        viewModelScope.launch {
            uiState = uiState.copy(isRestoring = true)
            backupRepository.startRestore(
                backupItems = backupItems ?: return@launch,
                platformFile = platformFile ?: return@launch,
                includeFavorites = includeFavorites,
                includeBlacklisted = includeBlacklisted,
                includeSettings = includeSettings,
                includeSearchHistory = includeSearchHistory,
                listItemsByUuid = listsToInclude,
            )
            delay(1000)
            toasterState.show(
                "Running Restore in Background",
                type = ToastType.Info
            )
            uiState = uiState.copy(isRestoring = false)
            navigationHandler.backStack.removeLastOrNull()
        }
    }

    fun addList(uuid: String) {
        if (uuid !in listsToInclude) {
            listsToInclude.add(uuid)
        }
    }

    fun removeList(uuid: String) {
        listsToInclude.remove(uuid)
    }
}

data class RestoreUiState(
    val isReading: Boolean,
    val isRestoring: Boolean,
    val error: Throwable?,
)
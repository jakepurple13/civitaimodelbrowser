package com.programmersbox.common.backup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import com.programmersbox.common.di.NavigationHandler
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
    val listsToInclude = mutableStateListOf<String>()

    var uiState by mutableStateOf(
        RestoreUiState(
            isReading = false,
            isRestoring = false,
            error = null
        )
    )

    //TODO: Need to include error state

    fun read(platformFile: PlatformFile) {
        viewModelScope.launch {
            uiState = uiState.copy(isReading = true)
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
        viewModelScope.launch {
            uiState = uiState.copy(isRestoring = true)
            backupItems
                ?.copy(lists = backupItems?.lists?.filter { it.item.uuid in listsToInclude })
                ?.let {
                    backupRepository.restoreItems(
                        backupItems = it,
                        includeFavorites = includeFavorites,
                        includeBlacklisted = includeBlacklisted,
                        includeSettings = includeSettings,
                    )
                }
            uiState = uiState.copy(isRestoring = false)
            toasterState.show(
                "Backup Complete",
                type = ToastType.Success
            )
            delay(1000)
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
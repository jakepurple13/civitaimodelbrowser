package com.programmersbox.common.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import com.programmersbox.common.db.SearchHistoryDao
import com.programmersbox.common.di.NavigationHandler
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class BackupViewModel(
    private val backupRepository: BackupRepository,
    favoriteDao: FavoritesDao,
    listDao: ListDao,
    searchHistoryDao: SearchHistoryDao,
    private val navigationHandler: NavigationHandler,
    private val toasterState: ToasterState,
) : ViewModel() {
    val favoritesCount = favoriteDao.getFavoritesCount()
    val blacklistedCount = favoriteDao.getBlacklistCount()
    val lists = listDao.getAllLists()
    val searchHistoryCount = searchHistoryDao.getSearchCount()

    val backupItems: StateFlow<BackupItemsState>
        field = MutableStateFlow(
            BackupItemsState(
                includeFavorites = true,
                includeBlacklisted = true,
                includeSettings = true,
                includeSearchHistory = true,
                listsToInclude = persistentListOf()
            )
        )

    val uiState: StateFlow<BackupUiState>
        field = MutableStateFlow(BackupUiState(false, null))

    init {
        lists
            .onEach { customLists ->
                backupItems.value = backupItems
                    .value
                    .copy(
                        listsToInclude = customLists
                            .map { it.item.uuid }
                            .toPersistentList()
                    )
            }
            .launchIn(viewModelScope)
    }

    fun addList(item: String) {
        if (item !in backupItems.value.listsToInclude) {
            backupItems.value = backupItems
                .value
                .copy(
                    listsToInclude = persistentListOf(
                        *backupItems
                            .value
                            .listsToInclude
                            .toTypedArray(),
                        item
                    )
                )
        }
    }

    fun removeList(item: String) {
        backupItems.value = backupItems
            .value
            .copy(listsToInclude = (backupItems.value.listsToInclude - item).toPersistentList())
    }

    fun includeFavorites(includeFavorites: Boolean) {
        backupItems.value = backupItems.value.copy(includeFavorites = includeFavorites)
    }

    fun includeBlacklisted(includeBlacklisted: Boolean) {
        backupItems.value = backupItems.value.copy(includeBlacklisted = includeBlacklisted)
    }

    fun includeSettings(includeSettings: Boolean) {
        backupItems.value = backupItems.value.copy(includeSettings = includeSettings)
    }

    fun includeSearchHistory(includeSearchHistory: Boolean) {
        backupItems.value = backupItems.value.copy(includeSearchHistory = includeSearchHistory)
    }

    fun backup(
        platformFile: PlatformFile,
    ) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isBackingUp = true, error = null)
            runCatching {
                /*backupRepository.packageItems(
                    platformFile = platformFile,
                    includeFavorites = includeFavorites,
                    includeBlacklisted = includeBlacklisted,
                    includeSettings = includeSettings,
                    listItemsByUuid = listsToInclude
                )*/
                backupRepository.packageItems(
                    platformFile = platformFile,
                    includeFavorites = backupItems.value.includeFavorites,
                    includeBlacklisted = backupItems.value.includeBlacklisted,
                    includeSettings = backupItems.value.includeSettings,
                    includeSearchHistory = backupItems.value.includeSearchHistory,
                    listItemsByUuid = backupItems.value.listsToInclude
                )
            }
                .onSuccess {
                    uiState.value = uiState.value.copy(isBackingUp = false, error = null)
                    toasterState.show(
                        "Backup Complete",
                        type = ToastType.Success
                    )
                    navigationHandler.backStack.removeLastOrNull()
                }
                .onFailure {
                    it.printStackTrace()
                    uiState.value = uiState.value.copy(isBackingUp = false, error = it)
                    toasterState.show(
                        "Backup Failed",
                        type = ToastType.Error
                    )
                }
        }
    }
}

@Serializable
data class BackupItemsState(
    val includeFavorites: Boolean,
    val includeBlacklisted: Boolean,
    val includeSettings: Boolean,
    val includeSearchHistory: Boolean,
    val listsToInclude: ImmutableList<String>,
)

data class BackupUiState(
    val isBackingUp: Boolean,
    val error: Throwable?,
)
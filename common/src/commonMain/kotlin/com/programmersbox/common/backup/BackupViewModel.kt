package com.programmersbox.common.backup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.NavigationHandler
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BackupViewModel(
    private val backupRepository: BackupRepository,
    favoriteDao: FavoritesDao,
    listDao: ListDao,
    private val navigationHandler: NavigationHandler
) : ViewModel() {
    val favoritesCount = favoriteDao.getFavoritesCount()
    val blacklistedCount = favoriteDao.getBlacklistCount()
    val lists = listDao.getAllLists()

    var includeFavorites by mutableStateOf(true)
    var includeBlacklisted by mutableStateOf(true)
    var includeSettings by mutableStateOf(true)
    val listsToInclude = mutableStateListOf<String>()

    var isBackingUp by mutableStateOf(false)

    init {
        lists
            .onEach { customLists ->
                listsToInclude.clear()
                listsToInclude.addAll(customLists.map { it.item.uuid })
            }
            .launchIn(viewModelScope)
    }

    fun addList(uuid: String) {
        if (uuid !in listsToInclude) {
            listsToInclude.add(uuid)
        }
    }

    fun removeList(uuid: String) {
        listsToInclude.remove(uuid)
    }

    fun backup(
        platformFile: PlatformFile,
    ) {
        viewModelScope.launch {
            isBackingUp = true
            backupRepository.packageItems(
                platformFile = platformFile,
                includeFavorites = includeFavorites,
                includeBlacklisted = includeBlacklisted,
                includeSettings = includeSettings,
                listItemsByUuid = listsToInclude
            )
            isBackingUp = false
            delay(1000)
            navigationHandler.backStack.removeLastOrNull()
        }
    }
}
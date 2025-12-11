package com.programmersbox.common.lists

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.CustomListInfo
import com.programmersbox.common.db.ListDao
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ListDetailViewModel(
    private val listDao: ListDao,
    uuid: String,
) : ViewModel() {
    var customList by mutableStateOf<CustomList?>(null)

    private val itemList = mutableStateListOf<CustomListInfo>()

    val search = TextFieldState("")

    val searchedList by derivedStateOf {
        itemList.filter { it.name.contains(search.text, true) }
    }

    init {
        listDao.getCustomListItemFlow(uuid)
            .onEach {
                customList = it
                itemList.clear()
                itemList.addAll(it.list)
            }
            .launchIn(viewModelScope)
    }

    suspend fun removeItems(items: List<CustomListInfo>): Result<Boolean> = runCatching {
        items.forEach { item -> listDao.removeItem(item) }
        customList?.item?.let { listDao.updateFullList(it) }
        true
    }

    fun rename(newName: String) {
        viewModelScope.launch {
            customList?.item?.copy(name = newName)?.let { listDao.updateFullList(it) }
        }
    }

    fun deleteAll() {
        viewModelScope.launch { customList?.let { item -> listDao.removeList(item) } }
    }
}
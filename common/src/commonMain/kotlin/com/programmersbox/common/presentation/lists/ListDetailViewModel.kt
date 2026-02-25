package com.programmersbox.common.presentation.lists

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
import com.programmersbox.common.db.ListRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ListDetailViewModel(
    private val listRepository: ListRepository,
    uuid: String,
) : ViewModel() {
    var customList by mutableStateOf<CustomList?>(null)

    private val itemList = mutableStateListOf<CustomListInfo>()

    val search = TextFieldState("")

    val searchedList by derivedStateOf {
        itemList.filter {
            it.name.contains(search.text, true) ||
                    it.description?.contains(search.text, true) == true
        }
    }

    init {
        listRepository
            .getCustomListItemFlow(uuid)
            .onEach {
                customList = it
                itemList.clear()
                itemList.addAll(it.list)
            }
            .launchIn(viewModelScope)
    }

    suspend fun removeItems(items: List<CustomListInfo>): Result<Boolean> = runCatching {
        items.forEach { item -> listRepository.removeItem(item) }
        customList?.item?.let { listRepository.updateFullList(it) }
        true
    }

    fun rename(newName: String) {
        viewModelScope.launch {
            customList?.item?.copy(name = newName)?.let { listRepository.updateFullList(it) }
        }
    }

    fun setDescription(newDescription: String?) {
        viewModelScope.launch {
            customList?.item?.copy(description = newDescription)
                ?.let { listRepository.updateFullList(it) }
        }
    }

    fun setBiometric(useBiometric: Boolean) {
        viewModelScope.launch {
            customList?.item?.uuid?.let { listRepository.updateBiometric(it, useBiometric) }
        }
    }

    fun deleteAll() {
        viewModelScope.launch { customList?.let { item -> listRepository.removeList(item) } }
    }

    fun setCoverImage(url: String?, hash: String?) {
        viewModelScope.launch {
            customList?.item?.uuid?.let { listRepository.updateCoverImage(it, url, hash) }
        }
    }
}
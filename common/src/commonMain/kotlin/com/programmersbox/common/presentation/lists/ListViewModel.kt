package com.programmersbox.common.presentation.lists

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.ListDao
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ListViewModel(
    val listDao: ListDao,
) : ViewModel() {

    var list by mutableStateOf<List<CustomList>>(emptyList())
    var search by mutableStateOf("")
    var searchType by mutableStateOf(SearchType.SQL)

    private val searchLists = mutableStateListOf<CustomList>()

    init {
        listDao
            .getAllLists()
            .onEach { list = it }
            .launchIn(viewModelScope)

        combine(
            snapshotFlow { search },
            snapshotFlow { searchType }
        ) { search, searchType -> search to searchType }
            .distinctUntilChanged()
            .flatMapLatest {
                if (it.second != SearchType.SQL) {
                    flowOf(emptyList())
                } else {
                    listDao.search(it.first)
                }
            }
            .onEach {
                searchLists.clear()
                searchLists.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    val searchList by derivedStateOf {
        if (search.isEmpty()) list
        else {
            when (searchType) {
                SearchType.DFS -> list.dfsSearch(search)
                SearchType.BFS -> list.bfsSearch(search)
                SearchType.SQL -> searchLists
            }.distinctBy { it.item.uuid }
        }
    }
}
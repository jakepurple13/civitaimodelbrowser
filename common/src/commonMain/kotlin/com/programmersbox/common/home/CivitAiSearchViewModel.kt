package com.programmersbox.common.home

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.programmersbox.common.DataStore
import com.programmersbox.common.Models
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT
import com.programmersbox.common.db.SearchHistoryDao
import com.programmersbox.common.db.SearchHistoryItem
import com.programmersbox.common.paging.CivitBrowserSearchPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class CivitAiSearchViewModel @OptIn(ExperimentalMaterial3Api::class) constructor(
    private val network: Network,
    private val dataStore: DataStore,
    private val searchHistoryDao: SearchHistoryDao,
) : ViewModel() {
    val searchQuery = TextFieldState("")

    var pager by mutableStateOf<Flow<PagingData<Models>>>(
        flowOf(
            PagingData.empty(
                LoadStates(
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                )
            )
        )
    )

    val searchFlow = snapshotFlow { searchQuery.text }
        .flatMapLatest {
            if (it.isEmpty()) {
                searchHistoryDao.getAllSearchHistoryFlow()
            } else {
                searchHistoryDao.getSearchHistory(it.toString())
            }
        }

    fun onSearch(query: String) {
        viewModelScope.launch {
            pager = if (query.isEmpty()) {
                flowOf(
                    PagingData.empty(
                        LoadStates(
                            LoadState.NotLoading(true),
                            LoadState.NotLoading(true),
                            LoadState.NotLoading(true),
                        )
                    )
                )
            } else {
                searchHistoryDao.addSearchHistory(SearchHistoryItem(query))
                val includeNsfw = dataStore.includeNsfw.flow.first()
                Pager(
                    PagingConfig(
                        pageSize = PAGE_LIMIT,
                        enablePlaceholders = true
                    ),
                ) { CivitBrowserSearchPagingSource(network, query, includeNsfw) }
                    .flow
                    .flowOn(Dispatchers.IO)
                    .cachedIn(viewModelScope)
            }
        }
    }

    fun removeSearchHistory(item: SearchHistoryItem) {
        viewModelScope.launch {
            searchHistoryDao.removeSearchHistory(item)
        }
    }
}
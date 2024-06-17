package com.programmersbox.common.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.programmersbox.common.DataStore
import com.programmersbox.common.Models
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT
import com.programmersbox.common.paging.CivitBrowserPagingSource
import com.programmersbox.common.paging.CivitBrowserSearchPagingSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CivitAiViewModel(
    network: Network,
    dataStore: DataStore,
) : ViewModel() {

    var pager by mutableStateOf<Flow<PagingData<Models>>>(emptyFlow())

    init {
        dataStore.includeNsfw.flow
            .distinctUntilChanged()
            .onEach {
                pager = Pager(
                    PagingConfig(
                        pageSize = PAGE_LIMIT,
                        enablePlaceholders = true
                    ),
                ) { CivitBrowserPagingSource(network, it) }
                    .flow
                    .cachedIn(viewModelScope)
            }
            .launchIn(viewModelScope)
    }

}

class CivitAiSearchViewModel(
    private val network: Network,
    private val dataStore: DataStore,
) : ViewModel() {

    var showSearch by mutableStateOf(false)

    var searchQuery by mutableStateOf("")

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
                val includeNsfw = dataStore.includeNsfw.flow.first()
                Pager(
                    PagingConfig(
                        pageSize = PAGE_LIMIT,
                        enablePlaceholders = true
                    ),
                ) { CivitBrowserSearchPagingSource(network, query, includeNsfw) }
                    .flow
                    .cachedIn(viewModelScope)
            }
        }
    }
}
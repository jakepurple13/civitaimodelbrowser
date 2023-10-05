package com.programmersbox.common.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.programmersbox.common.DataStore
import com.programmersbox.common.Models
import com.programmersbox.common.Network
import com.programmersbox.common.paging.CivitBrowserPagingSource
import com.programmersbox.common.paging.CivitBrowserSearchPagingSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class CivitAiViewModel(
    network: Network,
    dataStore: DataStore,
): ViewModel() {

    var pager by mutableStateOf<Flow<PagingData<Models>>>(emptyFlow())

    init {
        dataStore.includeNsfw.flow
            .distinctUntilChanged()
            .onEach {
                pager = Pager(
                    PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = true
                    ),
                ) { CivitBrowserPagingSource(network, 20, it) }
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

    var pager by mutableStateOf<Flow<PagingData<Models>>>(emptyFlow())

    fun onSearch(query: String) {
        viewModelScope.launch {
            val includeNsfw = dataStore.includeNsfw.flow.first()
            pager = Pager(
                PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = true
                ),
            ) { CivitBrowserSearchPagingSource(network, query, 20, includeNsfw) }
                .flow
                .cachedIn(viewModelScope)
        }
    }
}
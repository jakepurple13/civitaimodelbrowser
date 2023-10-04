package com.programmersbox.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class CivitAiViewModel(
    network: Network,
): ViewModel() {

    val pager = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = true
        ),
    ) { CivitBrowserPagingSource(network, 20) }
        .flow
        .cachedIn(viewModelScope)
}

class CivitAiSearchViewModel(
    private val network: Network,
) : ViewModel() {

    var showSearch by mutableStateOf(false)

    var searchQuery by mutableStateOf("")

    var pager by mutableStateOf<Flow<PagingData<Models>>>(emptyFlow())

    fun onSearch(query: String) {
        pager = Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = true
            ),
        ) { CivitBrowserSearchPagingSource(network, query, 20) }
            .flow
            .cachedIn(viewModelScope)
    }
}
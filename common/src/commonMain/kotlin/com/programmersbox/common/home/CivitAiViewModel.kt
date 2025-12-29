package com.programmersbox.common.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.programmersbox.common.CivitSort
import com.programmersbox.common.DataStore
import com.programmersbox.common.Models
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT
import com.programmersbox.common.paging.CivitBrowserPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CivitAiViewModel(
    network: Network,
    dataStore: DataStore,
) : ViewModel() {
    val pager: Flow<PagingData<Models>>
        field = MutableStateFlow<PagingData<Models>>(PagingData.empty())

    var sort: CivitSort by mutableStateOf(CivitSort.Newest)

    init {
        combine(
            dataStore
            .includeNsfw
            .flow
                .distinctUntilChanged(),
            snapshotFlow { sort }
        ) { includeNsfw, sort -> includeNsfw to sort }
            .flatMapLatest {
                Pager(
                    PagingConfig(
                        pageSize = PAGE_LIMIT,
                        enablePlaceholders = true
                    ),
                ) {
                    CivitBrowserPagingSource(
                        network = network,
                        includeNsfw = it.first,
                        sort = it.second
                    )
                }
                    .flow
                    .flowOn(Dispatchers.IO)
                    .cachedIn(viewModelScope)
            }
            .onEach { pager.value = it }
            .launchIn(viewModelScope)
    }
}
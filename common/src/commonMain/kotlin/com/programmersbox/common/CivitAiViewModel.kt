package com.programmersbox.common

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class CivitAiViewModel(
    private val network: Network = Network()
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
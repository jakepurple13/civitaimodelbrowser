package com.programmersbox.common.creator

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.paging.CivitBrowserUserPagingSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class CivitAiUserViewModel(
    network: Network,
    dataStore: DataStore,
    val username: String,
) : ViewModel() {
    val pager = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = true
        ),
    ) {
        CivitBrowserUserPagingSource(
            network = network,
            username = username,
            includeNsfw = runBlocking { dataStore.includeNsfw.flow.first() }
        )
    }
        .flow
        .cachedIn(viewModelScope)
}
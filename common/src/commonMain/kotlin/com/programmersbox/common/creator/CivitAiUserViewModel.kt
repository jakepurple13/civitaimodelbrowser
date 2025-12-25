package com.programmersbox.common.creator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.programmersbox.common.Creator
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.paging.CivitBrowserUserPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class CivitAiUserViewModel(
    network: Network,
    dataStore: DataStore,
    val database: FavoritesDao,
    val username: String,
) : ViewModel() {
    val pager = Pager(
        PagingConfig(
            pageSize = PAGE_LIMIT,
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
        .flowOn(Dispatchers.IO)
        .cachedIn(viewModelScope)

    fun addToFavorites(
        creator: Creator,
    ) {
        viewModelScope.launch {
            database.addFavorite(
                id = database.getFavoritesSync().maxOf { it.id } + 1,
                name = creator.username.orEmpty(),
                imageUrl = creator.image,
                favoriteType = FavoriteType.Creator,
                modelId = Random.nextLong()
            )
        }
    }

    fun removeToFavorites(
        creator: Creator,
    ) {
        viewModelScope.launch {
            database.removeCreator(creator)
        }
    }
}
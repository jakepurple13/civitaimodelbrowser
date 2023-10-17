package com.programmersbox.common.creator

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.programmersbox.common.Creator
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDatabase
import com.programmersbox.common.paging.CivitBrowserUserPagingSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.random.Random

class CivitAiUserViewModel(
    network: Network,
    dataStore: DataStore,
    val database: FavoritesDatabase,
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

    fun addToFavorites(
        creator: Creator,
    ) {
        viewModelScope.launch {
            database.addFavorite(
                id = Random.nextLong(),
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
            database.removeFrom { removeIf { it.name == creator.username && it.favoriteType == FavoriteType.Creator.name } }
        }
    }
}
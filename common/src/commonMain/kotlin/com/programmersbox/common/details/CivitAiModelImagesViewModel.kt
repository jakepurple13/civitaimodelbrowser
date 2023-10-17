package com.programmersbox.common.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.*
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDatabase
import com.programmersbox.common.db.toDb
import com.programmersbox.common.paging.CivitDetailsImagePagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.random.Random


class CivitAiModelImagesViewModel(
    private val modelId: String?,
    dataStore: DataStore,
    network: Network,
    private val database: FavoritesDatabase,
) : ViewModel() {

    var pager by mutableStateOf<Flow<PagingData<CustomModelImage>>>(
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

    init {
        viewModelScope.launch {
            val includeNsfw = dataStore.includeNsfw.flow.first()
            pager = Pager(
                PagingConfig(
                    pageSize = PAGE_LIMIT,
                    enablePlaceholders = true
                ),
            ) { CivitDetailsImagePagingSource(network, includeNsfw, modelId) }
                .flow
                .cachedIn(viewModelScope)
        }
    }

    fun addImageToFavorites(modelImage: CustomModelImage) {
        viewModelScope.launch {
            database.addFavorite(
                id = modelImage.id?.toLongOrNull() ?: Random.nextLong(),
                name = modelImage.meta?.model.orEmpty(),
                imageMetaDb = modelImage.meta?.toDb(),
                nsfw = modelImage.nsfwLevel.canNotShow(),
                imageUrl = modelImage.url,
                favoriteType = FavoriteType.Image,
                modelId = modelId?.toLongOrNull() ?: modelImage.id?.toLongOrNull() ?: Random.nextLong()
            )
        }
    }

    fun removeImageFromFavorites(modelImage: CustomModelImage) {
        viewModelScope.launch {
            database.removeFrom {
                removeIf { f -> f.imageUrl == modelImage.url && f.favoriteType == FavoriteType.Image.name }
            }
        }
    }
}
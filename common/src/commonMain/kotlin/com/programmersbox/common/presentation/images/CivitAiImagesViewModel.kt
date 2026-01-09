package com.programmersbox.common.presentation.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.toDb
import com.programmersbox.common.paging.CivitImagePagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.random.Random

class CivitAiImagesViewModel(
    dataStore: DataStore,
    network: Network,
    private val database: FavoritesDao,
) : ViewModel() {
    val pager: Flow<PagingData<Pair<Long, List<CustomModelImage>>>>
        field = MutableStateFlow(PagingData.empty())

    val favoriteList = database
        .getFavoriteModels()
        .map { it.filterIsInstance<FavoriteModel.Image>() }

    init {
        dataStore.includeNsfw.flow
            .distinctUntilChanged()
            .flatMapLatest {
                Pager(
                    PagingConfig(
                        pageSize = PAGE_LIMIT,
                        enablePlaceholders = true
                    ),
                ) { CivitImagePagingSource(network, it) }
                    .flow
                    .flowOn(Dispatchers.IO)
                    .cachedIn(viewModelScope)
            }
            .onEach { pager.value = it }
            .launchIn(viewModelScope)
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
                modelId = Random.nextLong(),
                hash = modelImage.hash
            )
        }
    }

    fun removeImageFromFavorites(modelImage: CustomModelImage) {
        viewModelScope.launch {
            database.removeImage(modelImage.url)
        }
    }
}
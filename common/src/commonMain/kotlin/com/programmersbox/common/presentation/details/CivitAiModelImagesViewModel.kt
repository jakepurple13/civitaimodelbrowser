package com.programmersbox.common.presentation.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.Screen
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.toDb
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.random.Random

class CivitAiModelImagesViewModel(
    val modelDetailsImage: Screen.DetailsImage,
    dataStore: DataStore,
    network: Network,
    private val database: FavoritesDao,
) : ViewModel() {
    val imagesList = mutableStateListOf<CustomModelImage>()

    var uiState: ImageUiState by mutableStateOf(ImageUiState.Loading)

    val favoriteList = database
        .getFavoriteModels()
        .map { it.filterIsInstance<FavoriteModel.Image>() }

    init {
        val dispatcher = Dispatchers.IO.limitedParallelism(3)

        dataStore.includeNsfw.flow
            .map {
                uiState = ImageUiState.Loading
                coroutineScope {
                    if (modelDetailsImage.modelVersions != null) {
                        modelDetailsImage
                            .modelVersions
                            .toList()
                    } else {
                        network
                            .fetchModel(modelDetailsImage.modelId)
                            .getOrNull()
                            ?.modelVersions
                            ?.map { m -> m.id }
                            ?: return@coroutineScope emptyList()
                    }
                        .map { version ->
                            async(dispatcher, start = CoroutineStart.LAZY) {
                                network.fetchAllImagesByModelVersion(
                                    modelId = version.toString(),
                                    page = 1,
                                    perPage = 100,
                                    includeNsfw = it
                                )
                                    .getOrNull()
                                    ?.items
                            }
                        }
                        .awaitAll()
                        .filterNotNull()
                        .flatten()
                }
            }
            .flowOn(Dispatchers.IO)
            .catch { uiState = ImageUiState.Error(it) }
            .onEach {
                uiState = ImageUiState.Success
                imagesList.clear()
                imagesList.addAll(it)
            }
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
                hash = modelImage.hash,
                modelId = modelDetailsImage
                    .modelId
                    .toLongOrNull()
                    ?: modelImage.id?.toLongOrNull()
                    ?: Random.nextLong()
            )
        }
    }

    fun removeImageFromFavorites(modelImage: CustomModelImage) {
        viewModelScope.launch {
            database.removeImage(modelImage.url)
        }
    }
}

sealed class ImageUiState {
    data object Loading : ImageUiState()
    data object Success : ImageUiState()
    data class Error(val throwable: Throwable) : ImageUiState()
}
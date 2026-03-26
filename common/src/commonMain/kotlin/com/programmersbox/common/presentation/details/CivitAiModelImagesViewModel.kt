package com.programmersbox.common.presentation.details

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.DataStore
import com.programmersbox.common.Network
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.toDb
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

class CivitAiModelImagesViewModel(
    private val modelId: String?,
    dataStore: DataStore,
    network: Network,
    private val database: FavoritesDao,
) : ViewModel() {
    val imagesList = mutableStateListOf<CustomModelImage>()

    val favoriteList = database
        .getFavoriteModels()
        .map { it.filterIsInstance<FavoriteModel.Image>() }

    init {
        dataStore.includeNsfw.flow
            .map {
                imagesList.clear()
                coroutineScope {
                    val model = network
                        .fetchModel(modelId ?: return@coroutineScope)
                        .getOrNull() ?: return@coroutineScope

                    val versions = model
                        .modelVersions.map { m -> m.id }
                        .map { version ->
                            async {
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

                    imagesList.addAll(versions.flatten())
                }
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
                modelId = modelId?.toLongOrNull() ?: modelImage.id?.toLongOrNull()
                ?: Random.nextLong(),
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
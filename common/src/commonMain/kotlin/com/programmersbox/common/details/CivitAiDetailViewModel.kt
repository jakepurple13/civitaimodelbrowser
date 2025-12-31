package com.programmersbox.common.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.ModelImage
import com.programmersbox.common.Models
import com.programmersbox.common.Network
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.toDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import kotlin.random.Random

class CivitAiDetailViewModel(
    private val network: Network,
    private val id: String?,
    private val database: FavoritesDao,
) : ViewModel() {
    val modelUrl = "https://civitai.com/models/$id"
    var models by mutableStateOf<DetailViewState>(DetailViewState.Loading)
    val showMoreInfo = mutableStateMapOf<Long, Boolean>()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            models = DetailViewState.Loading
            models = id?.let { network.fetchModel(it) }
                ?.onFailure { it.printStackTrace() }
                ?.onSuccess {
                    showMoreInfo.clear()
                    it.modelVersions.forEachIndexed { index, mv ->
                        showMoreInfo[mv.id] = index == 0
                    }
                }
                ?.fold(
                    onSuccess = { DetailViewState.Content(it) },
                    onFailure = { DetailViewState.Error(it) }
                ) ?: DetailViewState.Error(Throwable("No ID"))
        }
    }

    fun toggleShowMoreInfo(id: Long) {
        showMoreInfo[id] = showMoreInfo[id]?.not() ?: false
    }

    fun addToFavorites() {
        viewModelScope.launch {
            (models as? DetailViewState.Content)?.models?.let { m ->
                val firstCapableImage = m
                    .modelVersions
                    .firstOrNull { it.images.isNotEmpty() }
                    ?.images
                    ?.firstOrNull()
                database.addFavorite(
                    id = m.id,
                    name = m.name,
                    description = m.description,
                    type = m.type,
                    nsfw = m.nsfw,
                    imageUrl = firstCapableImage?.url,
                    favoriteType = FavoriteType.Model,
                    modelId = m.id,
                    hash = firstCapableImage?.hash,
                    creatorName = m.creator?.username,
                    creatorImage = m.creator?.image,
                )
            }
        }
    }

    fun addImageToFavorites(modelImage: ModelImage) {
        viewModelScope.launch {
            database.addFavorite(
                id = modelImage.id?.toLongOrNull() ?: Random.nextLong(),
                name = modelImage.meta?.model ?: modelImage.url.toPath().name,
                imageMetaDb = modelImage.meta?.toDb(),
                nsfw = modelImage.nsfw.canNotShow(),
                imageUrl = modelImage.url,
                favoriteType = FavoriteType.Image,
                hash = modelImage.hash,
                modelId = (models as? DetailViewState.Content)?.models?.id
                    ?: modelImage.id?.toLongOrNull()
                    ?: Random.nextLong()
            )
        }
    }

    fun removeImageFromFavorites(modelImage: ModelImage) {
        viewModelScope.launch {
            database.removeImage(modelImage.url)
        }
    }

    fun removeFromFavorites() {
        viewModelScope.launch {
            (models as? DetailViewState.Content)?.models?.id?.let { database.removeModel(it) }
        }
    }
}

sealed class DetailViewState {
    data object Loading : DetailViewState()
    data class Error(val error: Throwable) : DetailViewState()
    data class Content(val models: Models) : DetailViewState()
}
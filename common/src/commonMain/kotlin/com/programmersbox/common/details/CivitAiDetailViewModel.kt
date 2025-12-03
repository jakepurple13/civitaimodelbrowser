package com.programmersbox.common.details

import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    var isFavorite by mutableStateOf(false)

    init {
        loadData()
        database.getFavoriteModels()
            .onEach { m -> isFavorite = m.any { it.id == id?.toLongOrNull() } }
            .launchIn(viewModelScope)
    }

    fun loadData() {
        viewModelScope.launch {
            models = DetailViewState.Loading
            models = id?.let { network.fetchModel(it) }
                ?.onFailure { it.printStackTrace() }
                ?.fold(
                    onSuccess = { DetailViewState.Content(it) },
                    onFailure = { DetailViewState.Error(it) }
                ) ?: DetailViewState.Error(Throwable("No ID"))
        }
    }

    fun addToFavorites() {
        viewModelScope.launch {
            (models as? DetailViewState.Content)?.models?.let { m ->
                database.addFavorite(
                    id = m.id,
                    name = m.name,
                    description = m.description,
                    type = m.type,
                    nsfw = m.nsfw,
                    imageUrl = m.modelVersions.firstOrNull()?.images?.firstOrNull()?.url,
                    favoriteType = FavoriteType.Model,
                    modelId = m.id,
                    hash = m.modelVersions.firstOrNull()?.images?.firstOrNull()?.hash
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
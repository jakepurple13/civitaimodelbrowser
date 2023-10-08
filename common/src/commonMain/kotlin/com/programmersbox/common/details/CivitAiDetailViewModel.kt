package com.programmersbox.common.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.programmersbox.common.ModelImage
import com.programmersbox.common.Models
import com.programmersbox.common.Network
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDatabase
import com.programmersbox.common.db.toDb
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.random.Random

class CivitAiDetailViewModel(
    private val network: Network,
    private val id: String?,
    private val database: FavoritesDatabase,
) : ViewModel() {
    val modelUrl = "https://civitai.com/models/$id"
    var models by mutableStateOf<DetailViewState>(DetailViewState.Loading)
    var isFavorite by mutableStateOf(false)

    init {
        loadData()
        database.getFavorites()
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
                    onFailure = { DetailViewState.Error }
                ) ?: DetailViewState.Error
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
                    favoriteType = FavoriteType.Model
                )
            }
        }
    }

    fun addImageToFavorites(modelImage: ModelImage) {
        viewModelScope.launch {
            database.addFavorite(
                id = modelImage.id?.toLongOrNull()
                    ?: (models as? DetailViewState.Content)?.models?.id
                    ?: Random.nextLong(),
                name = modelImage.meta?.model.orEmpty(),
                imageMetaDb = modelImage.meta?.toDb(),
                nsfw = modelImage.nsfw.canNotShow(),
                imageUrl = modelImage.url,
                favoriteType = FavoriteType.Image
            )
        }
    }

    fun removeImageFromFavorites(modelImage: ModelImage) {
        viewModelScope.launch {
            database.removeFrom {
                removeIf { f -> f.imageUrl == modelImage.url && f.favoriteType == FavoriteType.Image.name }
            }
        }
    }

    fun removeFromFavorites() {
        viewModelScope.launch {
            (models as? DetailViewState.Content)?.models?.id?.let {
                database.removeFrom {
                    removeIf { f -> f.id == it && f.favoriteType == FavoriteType.Model.name }
                }
            }
        }
    }
}

sealed class DetailViewState {
    data object Loading : DetailViewState()
    data object Error : DetailViewState()
    data class Content(val models: Models) : DetailViewState()
}
package com.programmersbox.common.db

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.DataStore
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val dao: FavoritesDao,
    dataStore: DataStore,
) : ViewModel() {
    var search by mutableStateOf("")
    val filterList = mutableStateListOf<String>()
    val favoritesList = mutableStateListOf<FavoriteModel>()

    var sortedBy by mutableStateOf(SortedBy.Default)

    val typeList by derivedStateOf {
        favoritesList
            .filterIsInstance<FavoriteModel.Model>()
            .map { it.type }
            .distinct()
    }

    private var includeNsfw by mutableStateOf(false)

    val viewingList by derivedStateOf {
        dao
            .searchForFavorites(
                query = search,
                type = filterList.takeIf { it.isNotEmpty() } ?: typeList,
                includeNsfw = includeNsfw
            )
            .map { list ->
                list
                    .filter {
                        filterList.isEmpty() || when (it) {
                            is FavoriteModel.Creator -> CREATOR_FILTER
                            is FavoriteModel.Image -> IMAGE_FILTER
                            is FavoriteModel.Model -> it.type
                        } in filterList
                    }
                    .let { sortedBy.sorting(it) }
            }
    }

    init {
        dataStore
            .includeNsfw
            .flow
            .onEach { includeNsfw = it }
            .flatMapLatest { includeNsfw ->
                dao.getFavoriteModels(includeNsfw = includeNsfw)
            }
            .onEach {
                favoritesList.clear()
                favoritesList.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    fun toggleFilter(filter: String) {
        if (filter in filterList) {
            filterList.remove(filter)
        } else {
            filterList.add(filter)
        }
    }

    fun removeImage(imageUrl: String) {
        viewModelScope.launch {
            dao.removeImage(imageUrl)
        }
    }
}

enum class SortedBy(
    val sorting: (List<FavoriteModel>) -> List<FavoriteModel>,
) {
    Default({ it }),
    Name({ it.sortedBy { it.name } }),
    Type(
        {
            it.sortedWith(
                compareByDescending<FavoriteModel> { it is FavoriteModel.Model }
                    .thenBy { it is FavoriteModel.Creator }
                    .thenBy { it is FavoriteModel.Image }
            )
        }
    )
}
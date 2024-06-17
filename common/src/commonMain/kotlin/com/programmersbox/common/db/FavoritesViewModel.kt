package com.programmersbox.common.db

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.DataStore
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class FavoritesViewModel(
    database: FavoritesDatabase,
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

    val viewingList by derivedStateOf {
        favoritesList
            .filter {
                val name = it.name.contains(search, true)
                val description = (it as? FavoriteModel.Model)
                    ?.description
                    ?.contains(search, true) == true
                val filter = filterList.isEmpty() || when (it) {
                    is FavoriteModel.Creator -> CREATOR_FILTER
                    is FavoriteModel.Image -> IMAGE_FILTER
                    is FavoriteModel.Model -> it.type
                } in filterList

                (name || description) && filter
            }
            .let { sortedBy.sorting(it) }
    }

    init {
        dataStore.reverseFavorites.flow
            .map { if (it) Sort.DESCENDING else Sort.ASCENDING }
            .flatMapLatest { database.getFavorites(it) }
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
package com.programmersbox.common.db

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.common.DataStore
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val dao: FavoritesDao,
    dataStore: DataStore,
) : ViewModel() {
    val search = TextFieldState("")
    val filterList = mutableStateListOf<String>()
    var sortedBy by mutableStateOf(SortedBy.Default)
    val typeList = dao.getTypes()
    private var includeNsfw by mutableStateOf(false)
    val viewingList by derivedStateOf {
        dao
            .searchForFavorites(
                query = search.text.toString(),
                type = filterList,
                includeNsfw = includeNsfw
            )
            .map { list -> sortedBy.sorting(list) }
    }

    init {
        dataStore
            .includeNsfw
            .flow
            .onEach { includeNsfw = it }
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
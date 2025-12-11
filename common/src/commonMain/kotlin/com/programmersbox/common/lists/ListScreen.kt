package com.programmersbox.common.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.LoadingImage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ListScreen(
    viewModel: ListViewModel = koinViewModel(),
) {
    Scaffold { padding ->
        LazyVerticalGrid(
            contentPadding = padding,
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                viewModel.list,
                key = { it.item.uuid }
            ) {
                LoadingImage(
                    imageUrl = it.item.coverImage ?: it.list.firstOrNull()?.imageUrl.orEmpty(),
                    name = it.item.name,
                    modifier = Modifier.size(
                        ComposableUtils.IMAGE_WIDTH,
                        ComposableUtils.IMAGE_HEIGHT
                    )
                )
            }
        }
    }
}
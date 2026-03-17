package com.programmersbox.common.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.programmersbox.common.CloseButton

/**
 * Represents a model for an item in a bottom sheet list, containing various text and icon attributes.
 *
 * @property primaryText The main text to be displayed for the item. This value is mandatory.
 * @property overlineText An optional text to be displayed above the primary text, typically in a smaller font.
 * @property secondaryText An optional text to be displayed below the primary text, providing additional details.
 * @property icon An optional graphical icon to be displayed alongside the text in the item.
 * @property trailingText An optional text to be displayed at the trailing end of the item, typically used for annotations or counts.
 */
class ListBottomSheetItemModel(
    val primaryText: String,
    val overlineText: String? = null,
    val secondaryText: String? = null,
    val icon: ImageVector? = null,
    val trailingText: String? = null,
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> ListBottomScreen(
    title: String,
    list: List<T>,
    onClick: (T) -> Unit,
    includeInsetPadding: Boolean = true,
    navigationIcon: @Composable () -> Unit = { CloseButton() },
    lazyListContent: LazyListScope.() -> Unit = {},
    itemContent: (T) -> ListBottomSheetItemModel,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        stickyHeader {
            TopAppBar(
                windowInsets = if (includeInsetPadding) WindowInsets.statusBars else WindowInsets(0.dp),
                title = { Text(title) },
                navigationIcon = navigationIcon,
                actions = { if (list.isNotEmpty()) Text("(${list.size})") }
            )
            HorizontalDivider()
        }
        lazyListContent()
        itemsIndexed(list) { index, it ->
            val c = itemContent(it)
            ListItem(
                modifier = Modifier.clickable { onClick(it) },
                leadingContent = c.icon?.let { i -> { Icon(i, null) } },
                headlineContent = { Text(c.primaryText) },
                supportingContent = c.secondaryText?.let { i -> { Text(i) } },
                overlineContent = c.overlineText?.let { i -> { Text(i) } },
                trailingContent = c.trailingText?.let { i -> { Text(i) } }
            )
            if (index < list.size - 1) HorizontalDivider()
        }
    }
}
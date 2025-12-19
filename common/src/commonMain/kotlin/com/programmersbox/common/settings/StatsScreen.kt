package com.programmersbox.common.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.materialkolor.ktx.blend
import com.programmersbox.common.BackButton
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.DataCounts
import com.programmersbox.common.db.FavoriteRoom
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import com.programmersbox.common.db.SearchHistoryDao
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val favoritesDao = koinInject<FavoritesDao>()
    val listDao = koinInject<ListDao>()
    val searchHistoryDao = koinInject<SearchHistoryDao>()

    val favoritesCount by favoritesDao
        .getTypeCounts()
        .collectAsStateWithLifecycle(DataCounts(0, 0, 0))

    val blacklistedCount by animateIntAsState(
        favoritesDao
            .getBlacklistCount()
            .collectAsStateWithLifecycle(0)
            .value
    )

    val listCount by animateIntAsState(
        listDao
            .getAllListsCount()
            .collectAsStateWithLifecycle(0)
            .value
    )

    val searchCount by searchHistoryDao
        .getSearchCount()
        .collectAsStateWithLifecycle(0)

    val itemCount by listDao
        .getTypeCounts()
        .collectAsStateWithLifecycle(DataCounts(0, 0, 0))

    val listItems by listDao
        .getAllLists()
        .collectAsStateWithLifecycle(emptyList())

    val favorites by favoritesDao
        .getFavorites()
        .collectAsStateWithLifecycle(emptyList())

    val blacklisted by favoritesDao
        .getBlacklisted()
        .collectAsStateWithLifecycle(emptyList())

    var showNsfwStats by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(contentType = "global stats") {
                GlobalStats(
                    favoritesCount = favoritesCount,
                    blacklistedCount = blacklistedCount,
                    listCount = listCount,
                    searchCount = searchCount,
                    modifier = Modifier.animateItem()
                )
            }

            item(contentType = "favorites deep dive") {
                DeepDive(
                    dataCounts = favoritesCount,
                    title = "Favorites Deep Dive",
                    modifier = Modifier.animateItem()
                )
            }

            item(contentType = "list title") {
                Text(
                    "List Stats",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .animateItem()
                        .padding(16.dp)
                )
            }

            item(contentType = "list deep dive") {
                DeepDive(
                    dataCounts = itemCount,
                    title = "Lists Deep Dive",
                    modifier = Modifier.animateItem()
                )
            }

            items(
                items = listItems,
                contentType = { "list" },
                key = { it.item.uuid }
            ) {
                ListStats(
                    list = it,
                    modifier = Modifier.animateItem()
                )
            }

            item(contentType = "nsfw") {
                NsfwStats(
                    lists = listItems.toImmutableList(),
                    favorites = favorites.toImmutableList(),
                    blacklisted = blacklisted.toImmutableList(),
                    showNsfwStats = showNsfwStats,
                    onShowNsfwStatsChange = { showNsfwStats = it },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun NsfwStats(
    lists: ImmutableList<CustomList>,
    favorites: ImmutableList<FavoriteRoom>,
    blacklisted: ImmutableList<BlacklistedItemRoom>,
    showNsfwStats: Boolean,
    onShowNsfwStatsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = modifier
            .animateContentSize()
            .padding(16.dp)
    ) {
        ListItem(
            headlineContent = { Text("Show NSFW Stats") },
            leadingContent = {
                Icon(
                    Icons.Default.NoAdultContent, null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            trailingContent = {
                Switch(
                    checked = showNsfwStats,
                    onCheckedChange = onShowNsfwStatsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                    )
                )
            },
        )
        AnimatedVisibility(showNsfwStats) {
            val primaryErrorContainer = MaterialTheme.colorScheme.primaryContainer
                .blend(MaterialTheme.colorScheme.errorContainer)

            val secondaryErrorContainer = MaterialTheme.colorScheme.secondaryContainer
                .blend(MaterialTheme.colorScheme.errorContainer)

            val tertiaryErrorContainer = MaterialTheme.colorScheme.tertiaryContainer
                .blend(MaterialTheme.colorScheme.errorContainer)

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlobalStatItem(
                        title = "Favorites",
                        value = favorites.filter { it.nsfw }.size,
                        color = primaryErrorContainer
                    )

                    GlobalStatItem(
                        title = "Blacklisted",
                        value = blacklisted.filter { it.nsfw }.size,
                        color = secondaryErrorContainer
                    )

                    GlobalStatItem(
                        title = "NSFW in Lists",
                        value = lists.sumOf { it.list.count { item -> item.nsfw } },
                        color = MaterialTheme.colorScheme.tertiaryContainer
                            .blend(MaterialTheme.colorScheme.errorContainer)
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    "Favorites",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    DeepDiveChip(
                        title = "Models",
                        value = favorites.filter { it.nsfw && it.favoriteType == FavoriteType.Model }.size,
                        icon = Icons.Default.ModelTraining,
                        color = primaryErrorContainer
                    )
                    DeepDiveChip(
                        title = "Images",
                        value = favorites.filter { it.nsfw && it.favoriteType == FavoriteType.Image }.size,
                        icon = Icons.Default.Image,
                        color = secondaryErrorContainer
                    )
                    DeepDiveChip(
                        title = "Creators",
                        value = favorites.filter { it.nsfw && it.favoriteType == FavoriteType.Creator }.size,
                        icon = Icons.Default.Person,
                        color = tertiaryErrorContainer
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    "Lists",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val listsNsfw = remember(lists) {
                        lists
                            .flatMap { it.list }
                            .filter { it.nsfw }
                            .groupBy { it.favoriteType }
                            .mapValues { it.value.size }
                    }

                    DeepDiveChip(
                        title = "Models",
                        value = listsNsfw[FavoriteType.Model] ?: 0,
                        icon = Icons.Default.ModelTraining,
                        color = primaryErrorContainer
                    )
                    DeepDiveChip(
                        title = "Images",
                        value = listsNsfw[FavoriteType.Image] ?: 0,
                        icon = Icons.Default.Image,
                        color = secondaryErrorContainer
                    )
                    DeepDiveChip(
                        title = "Creators",
                        value = listsNsfw[FavoriteType.Creator] ?: 0,
                        icon = Icons.Default.Person,
                        color = tertiaryErrorContainer
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    "Blacklisted",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                DeepDiveChip(
                    title = "Blacklisted",
                    value = blacklisted.filter { it.nsfw }.size,
                    icon = Icons.Default.Block,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ListStats(
    list: CustomList,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        ListItem(
            headlineContent = { Text(list.item.name) },
            trailingContent = { Text("(${list.list.size})") },
            supportingContent = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Models: ${list.list.filter { it.favoriteType == FavoriteType.Model }.size}")
                    Text("Images: ${list.list.filter { it.favoriteType == FavoriteType.Image }.size}")
                    Text("Creators: ${list.list.filter { it.favoriteType == FavoriteType.Creator }.size}")
                }
            },
            leadingContent = if (list.item.useBiometric) {
                {
                    Icon(
                        Icons.Default.Lock,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            } else null,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            )
        )
    }
}

@Composable
private fun DeepDive(
    dataCounts: DataCounts,
    title: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            val modelCount by animateIntAsState(dataCounts.modelCount)
            val imageCount by animateIntAsState(dataCounts.imageCount)
            val creatorCount by animateIntAsState(dataCounts.creatorCount)

            FlowRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DeepDiveChip(
                    title = "Models",
                    value = modelCount,
                    icon = Icons.Default.ModelTraining,
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                DeepDiveChip(
                    title = "Images",
                    value = imageCount,
                    icon = Icons.Default.Image,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )

                DeepDiveChip(
                    title = "Creators",
                    value = creatorCount,
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            // Segmented bar showing the distribution of favorite types
            // Feed raw counts (not animated) to avoid re-triggering animations each frame
            SegmentedBar(
                segments = persistentListOf(
                    Segment(
                        value = dataCounts.modelCount.toFloat(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ),
                    Segment(
                        value = dataCounts.imageCount.toFloat(),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    Segment(
                        value = dataCounts.creatorCount.toFloat(),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(54.dp),
                cornerRadius = 24.dp
            )
        }
    }
}

@Composable
private fun DeepDiveChip(
    title: String,
    value: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(icon, null)
            Text(title)
            Text(value.toString())
        }
    }
}

@Composable
private fun GlobalStats(
    favoritesCount: DataCounts,
    blacklistedCount: Int,
    listCount: Int,
    searchCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(4.dp)
    ) {
        Text(
            "Global Stats",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            item(
                contentType = "favorites"
            ) {
                GlobalStatItem(
                    title = "Favorites",
                    value = favoritesCount.imageCount
                            + favoritesCount.modelCount
                            + favoritesCount.creatorCount,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }

            item(
                contentType = "blacklisted"
            ) {
                GlobalStatItem(
                    title = "Blacklisted",
                    value = blacklistedCount,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            item(
                contentType = "lists"
            ) {
                GlobalStatItem(
                    title = "Total Lists",
                    value = listCount,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            item(
                contentType = "searches"
            ) {
                GlobalStatItem(
                    title = "Searches",
                    value = searchCount,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
        }
    }
}

@Composable
private fun GlobalStatItem(
    title: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                animateIntAsState(value).value.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

// ——————————————————————————————————————————————————————————————
// Segmented Bar Composable
@Stable
private data class Segment(
    val value: Float,
    val color: Color,
)

@Composable
private fun SegmentedBar(
    segments: PersistentList<Segment>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp,
    animate: Boolean = true,
    durationMillis: Int = 400,
    easing: Easing = FastOutSlowInEasing,
) {
    val total by remember(segments) {
        derivedStateOf { segments.sumOf { it.value.toDouble() }.toFloat() }
    }
    val safeSegments = if (total <= 0f) emptyList() else segments

    // Target fractions for each segment based on current data
    val targetFractions by remember(segments, total) {
        derivedStateOf {
            if (safeSegments.isEmpty()) emptyList() else safeSegments.map { s ->
                if (total == 0f) 0f else (s.value / total)
            }
        }
    }

    // Animatable fractions remembered by segment count only to avoid recreation on benign recompositions
    val animatedFractions = remember(safeSegments.size) {
        List(safeSegments.size) {
            // Start from 0f so the first visible frame animates from empty to target
            Animatable(0f)
        }
    }

    // Drive animations to new targets on change; animate together without restart
    LaunchedEffect(targetFractions, animate, durationMillis, easing) {
        if (!animate) {
            // Snap directly to target values when animation disabled
            animatedFractions.forEachIndexed { i, a ->
                a.snapTo(targetFractions.getOrNull(i) ?: 0f)
            }
        } else if (animatedFractions.isNotEmpty()) {
            coroutineScope {
                animatedFractions.forEachIndexed { i, a ->
                    val target = targetFractions.getOrNull(i) ?: 0f
                    launch {
                        a.animateTo(
                            target,
                            animationSpec = tween(durationMillis = durationMillis, easing = easing)
                        )
                    }
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        // Draw contiguous segments on a Canvas clipped by the rounded outer shape.
        Canvas(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            if (safeSegments.isEmpty()) return@Canvas

            val barWidth = size.width
            val barHeight = size.height

            // Use running x and ensure the last segment fills residual pixels to avoid seams.
            var x = 0f
            safeSegments.forEachIndexed { index, s ->
                val fraction = if (animatedFractions.getOrNull(index) != null)
                    animatedFractions[index].value
                else
                    targetFractions.getOrNull(index) ?: 0f
                val isLast = index == safeSegments.lastIndex
                val w = if (isLast) barWidth - x else barWidth * fraction
                if (w <= 0f) return@forEachIndexed
                drawRect(
                    color = s.color,
                    topLeft = Offset(x, 0f),
                    size = Size(w, barHeight)
                )
                x += w
            }
        }
    }
}
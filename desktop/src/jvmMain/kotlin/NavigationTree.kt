import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.programmersbox.common.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationTree(
    onCloseRequest: () -> Unit,
) {
    val navTree = remember { createNavigationGraph() }
    val maxDepth = remember { findMaxDepth(navTree) }
    WindowWithBar(
        windowTitle = "Navigation Tree",
        onCloseRequest = onCloseRequest
    ) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                MaxDepthInfoItem(maxDepth)
                HorizontalDivider()
                Graph(navTree)
            }
        }
    }
}

@Composable
private fun MaxDepthInfoItem(
    maxDepthInfo: MaxDepthInfo
) {
    Column {
        Text("Max Depth: ${maxDepthInfo.maxDepth}")
        Text("Path: ${maxDepthInfo.list.joinToString(" -> ") { it.toString() }}")
    }
}

@Composable
private fun Graph(
    node: NavNode,
) {
    Column {
        var showChildren by remember { mutableStateOf(false) }

        TextButton(
            onClick = { showChildren = !showChildren }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowRight,
                null,
                modifier = Modifier.rotate(
                    animateFloatAsState(if (showChildren) 90f else 0f).value
                )
            )
            Text(node.key.toString())
            Text("(${node.children.size})")
        }

        AnimatedVisibility(showChildren) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                node.children.forEach { child ->
                    Graph(child)
                }
                HorizontalDivider()
            }
        }
    }
}

@Stable
data class NavNode(
    val key: NavKey,
    val children: MutableList<NavNode> = mutableListOf(),
) {
    fun add(child: NavNode) {
        children.add(child)
    }
}

@Stable
data class MaxDepthInfo(
    val maxDepth: Int,
    val list: List<NavKey>
)

fun findMaxDepth(
    node: NavNode,
    set: MutableSet<NavKey> = mutableSetOf()
): MaxDepthInfo {
    if (node.key in set) return MaxDepthInfo(node.children.size, node.children.map { it.key })
    set.add(node.key)
    val max = node
        .children
        .map { findMaxDepth(it, set) }
        .maxByOrNull { it.maxDepth }
        ?: MaxDepthInfo(0, emptyList())

    println("${node.key} -> $max")

    return MaxDepthInfo(
        1 + max.maxDepth,
        listOf(node.key) + max.list
    )
}

fun createNavigationGraph(): NavNode {
    val root = NavNode(Screen.List)
    val favorites = NavNode(Screen.Favorites)
    val settings = NavNode(Screen.Settings)
    val blacklisted = NavNode(Screen.Settings.Blacklisted)
    val nsfw = NavNode(Screen.Settings.Nsfw)
    val behavior = NavNode(Screen.Settings.Behavior)
    val backup = NavNode(Screen.Settings.Backup)
    val restore = NavNode(Screen.Settings.Restore)
    val stats = NavNode(Screen.Settings.Stats)
    val about = NavNode(Screen.Settings.About)
    val search = NavNode(Screen.Search)
    val user = NavNode(Screen.User("Test"))
    val details = NavNode(Screen.Detail("Test"))
    val images = NavNode(Screen.Images)
    val qrCode = NavNode(Screen.QrCode)
    val customList = NavNode(Screen.CustomList)
    val customListDetail = NavNode(Screen.CustomListDetail("Test"))
    val webView = NavNode(Screen.WebView("Test"))
    val onboarding = NavNode(Screen.Onboarding)
    val detailsImage = NavNode(Screen.DetailsImage("Test", "Test"))

    settings.add(nsfw)
    settings.add(behavior)
    settings.add(backup)
    settings.add(restore)
    settings.add(stats)
    settings.add(about)
    settings.add(onboarding)
    settings.add(webView)

    customList.add(customListDetail)
    customListDetail.add(details)
    customListDetail.add(user)

    details.add(detailsImage)
    details.add(user)

    user.add(details)

    search.add(details)
    search.add(user)
    search.add(detailsImage)

    qrCode.add(details)

    favorites.add(details)
    favorites.add(user)

    images.add(user)

    root.add(favorites)
    root.add(settings)
    root.add(search)
    root.add(user)
    root.add(details)
    root.add(images)
    root.add(qrCode)
    root.add(customList)
    root.add(blacklisted)

    return root
}
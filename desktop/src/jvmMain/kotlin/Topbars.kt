import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

val LocalWindow: ProvidableCompositionLocal<FrameWindowScope> = staticCompositionLocalOf { error("None Yet") }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun WindowWithBar(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    canClose: Boolean = true,
    visible: Boolean = true,
    windowTitle: String = "",
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBar: @Composable () -> Unit = {},
    frameWindowScope: @Composable (FrameWindowScope.() -> Unit) = {},
    content: @Composable () -> Unit,
) {
    Window(
        state = state,
        undecorated = true,
        transparent = true,
        onCloseRequest = onCloseRequest,
        visible = visible,
    ) {
        CustomMaterialTheme {
            CompositionLocalProvider(LocalWindow provides this) {
                frameWindowScope()
                val hasFocus = LocalWindowInfo.current.isWindowFocused
                Surface(
                    shape = when (hostOs) {
                        OS.Linux -> RoundedCornerShape(8.dp)
                        OS.Windows -> RectangleShape
                        OS.MacOS -> RoundedCornerShape(8.dp)
                        else -> RoundedCornerShape(8.dp)
                    },
                    modifier = Modifier.animateContentSize(),
                    border = ButtonDefaults.outlinedButtonBorder,
                ) {
                    Scaffold(
                        topBar = {
                            Column {
                                WindowDraggableArea(
                                    modifier = Modifier.combinedClickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {},
                                        onDoubleClick = {
                                            state.placement =
                                                if (state.placement != WindowPlacement.Maximized) {
                                                    WindowPlacement.Maximized
                                                } else {
                                                    WindowPlacement.Floating
                                                }
                                        }
                                    )
                                ) {
                                    Surface(
                                        color = animateColorAsState(
                                            if (hasFocus) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ).value,
                                        tonalElevation = 0.dp
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .padding(horizontal = 4.dp)
                                                .padding(TopAppBarDefaults.windowInsets.asPaddingValues())
                                                .height(56.dp)
                                        ) {
                                            when (hostOs) {
                                                OS.Linux -> LinuxTopBar(state, onCloseRequest, windowTitle, canClose)
                                                OS.Windows -> WindowsTopBar(
                                                    state,
                                                    onCloseRequest,
                                                    windowTitle,
                                                    canClose
                                                )

                                                OS.MacOS -> MacOsTopBar(state, onCloseRequest, windowTitle, canClose)
                                                else -> {}
                                            }
                                        }
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        bottomBar = bottomBar,
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState,
                                snackbar = { Snackbar(snackbarData = it) }
                            )
                        }
                    ) { padding -> Surface(modifier = Modifier.padding(padding)) { content() } }
                }
            }
        }
    }
}

@Composable
private fun LinuxTopBar(state: WindowState, onExit: () -> Unit, windowTitle: String = "", canClose: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.Start
        ) {
            val hoverInteraction = remember { MutableInteractionSource() }
            val isHovering by hoverInteraction.collectIsHoveredAsState()

            val modifier = Modifier
                .weight(1f, false)
                .width(60.dp)
                .hoverable(hoverInteraction)

            CloseButton(onExit, modifier, isHovering, canClose)

            MinimizeButton(
                onMinimize = { state.isMinimized = !state.isMinimized },
                modifier, isHovering
            )

            MaximizeButton(
                onMaximize = {
                    state.placement = if (state.placement != WindowPlacement.Maximized) WindowPlacement.Maximized
                    else WindowPlacement.Floating
                },
                icon = Icons.Default.Maximize,
                modifier, isHovering
            )
        }

        Text(
            windowTitle,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }
}

@Composable
private fun WindowsTopBar(state: WindowState, onExit: () -> Unit, windowTitle: String = "", canClose: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.Start
        ) {
            val hoverInteraction = remember { MutableInteractionSource() }
            val isHovering by hoverInteraction.collectIsHoveredAsState()
            val modifier = Modifier
                .weight(1f, false)
                .width(60.dp)
                .hoverable(hoverInteraction)

            CloseButton(onExit, modifier, isHovering, canClose)

            MinimizeButton(
                onMinimize = { state.isMinimized = !state.isMinimized },
                modifier, isHovering
            )

            MaximizeButton(
                onMaximize = {
                    state.placement = if (state.placement != WindowPlacement.Maximized) WindowPlacement.Maximized
                    else WindowPlacement.Floating
                },
                icon = Icons.Default.Maximize,
                modifier, isHovering
            )
        }

        Text(
            windowTitle,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun MacOsTopBar(state: WindowState, onExit: () -> Unit, windowTitle: String = "", canClose: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.Start
        ) {
            val hoverInteraction = remember { MutableInteractionSource() }
            val isHovering by hoverInteraction.collectIsHoveredAsState()
            val modifier = Modifier
                .weight(1f, false)
                .width(60.dp)
                .hoverable(hoverInteraction)

            CloseButton(onExit, modifier, isHovering, canClose)

            MinimizeButton(
                onMinimize = { state.isMinimized = !state.isMinimized },
                modifier, isHovering
            )

            MaximizeButton(
                onMaximize = {
                    state.placement = if (state.placement != WindowPlacement.Fullscreen) WindowPlacement.Fullscreen
                    else WindowPlacement.Floating
                },
                icon = if (state.placement != WindowPlacement.Fullscreen) Icons.Default.Fullscreen else Icons.Default.FullscreenExit,
                modifier, isHovering
            )
        }

        Text(
            windowTitle,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun RowScope.CloseButton(
    onExit: () -> Unit,
    modifier: Modifier,
    isHovering: Boolean,
    canClose: Boolean,
) {
    NavigationBarItem(
        selected = false,
        onClick = onExit,
        enabled = canClose,
        modifier = modifier,
        icon = {
            Icon(
                Icons.Default.Close,
                null,
                tint = animateColorAsState(if (isHovering && canClose) Color.Red else LocalContentColor.current).value
            )
        },
        alwaysShowLabel = false,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Red,
            unselectedIconColor = LocalContentColor.current,
            indicatorColor = Color.Red.copy(alpha = .5f)
        ),
    )
}

@Composable
private fun RowScope.MinimizeButton(
    onMinimize: () -> Unit,
    modifier: Modifier,
    isHovering: Boolean,
) {
    NavigationBarItem(
        selected = false,
        onClick = onMinimize,
        modifier = modifier,
        icon = {
            Icon(
                Icons.Default.Minimize,
                null,
                tint = animateColorAsState(if (isHovering) Color.Yellow else LocalContentColor.current).value
            )
        },
        alwaysShowLabel = false,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Yellow,
            unselectedIconColor = LocalContentColor.current,
            indicatorColor = Color.Yellow.copy(alpha = .5f)
        ),
    )
}

@Composable
private fun RowScope.MaximizeButton(
    onMaximize: () -> Unit,
    icon: ImageVector,
    modifier: Modifier,
    isHovering: Boolean,
) {
    NavigationBarItem(
        selected = false,
        onClick = onMaximize,
        modifier = modifier,
        icon = {
            Icon(
                icon,
                null,
                tint = animateColorAsState(if (isHovering) Color.Green else LocalContentColor.current).value
            )
        },
        alwaysShowLabel = false,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Green,
            unselectedIconColor = LocalContentColor.current,
            indicatorColor = Color.Green.copy(alpha = .5f)
        ),
    )
}
package com.programmersbox.common.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitTopAppBar(
    showBlur: Boolean = false,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (showBlur)
            Color.Transparent
        else
            MaterialTheme.colorScheme.surface,
        scrolledContainerColor = if (showBlur)
            Color.Transparent
        else
            MaterialTheme.colorScheme.surface,
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    contentPadding: PaddingValues = TopAppBarDefaults.ContentPadding,
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        contentPadding = contentPadding,
        windowInsets = windowInsets,
        expandedHeight = expandedHeight,
        modifier = modifier,
    )
}
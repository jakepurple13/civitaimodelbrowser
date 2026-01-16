package com.programmersbox.common.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.programmersbox.common.DataStore
import com.programmersbox.common.presentation.onboarding.topics.FinishContent
import com.programmersbox.common.presentation.onboarding.topics.WelcomeContent
import com.programmersbox.common.presentation.settings.BehaviorSettings
import com.programmersbox.common.presentation.settings.NsfwSettings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
) {
    val dataStore = koinInject<DataStore>()

    val hasSeenOnboarding = dataStore.hasGoneThroughOnboarding

    val scope = rememberCoroutineScope()
    val onboardingScope = rememberOnboardingScope {
        item { WelcomeContent() }
        item { NsfwOnboarding(dataStore) }
        item { BehaviorOnboarding(dataStore) }
        item { FinishContent() }
    }
    val pagerState = rememberPagerState { onboardingScope.size }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Welcome!") },
                    subtitle = { Text("Civit Ai Model Browser") },
                    actions = {
                        var skipOnboarding by remember { mutableStateOf(false) }

                        if (skipOnboarding) {
                            AlertDialog(
                                onDismissRequest = { skipOnboarding = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            scope.launch { hasSeenOnboarding.update(true) }
                                                .invokeOnCompletion {
                                                    skipOnboarding = false
                                                    onFinish()
                                                }
                                        }
                                    ) { Text("Confirm") }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { skipOnboarding = false }
                                    ) { Text("Dismiss") }
                                },
                                title = { Text("Skip Onboarding?") },
                                text = { Text("Are you sure you want to skip onboarding?") }
                            )
                        }

                        TextButton(
                            onClick = { skipOnboarding = true }
                        ) { Text("Skip") }
                    }
                )
                HorizontalDivider()
                Spacer(Modifier.size(4.dp))
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    TextButton(
                        onClick = {
                            if (pagerState.canScrollBackward) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        },
                        enabled = pagerState.canScrollBackward
                    ) { Text("Back") }

                    OnboardingIndicator(pagerState)
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (pagerState.canScrollForward) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            } else {
                                scope.launch { hasSeenOnboarding.update(true) }
                                    .invokeOnCompletion { onFinish() }
                            }
                        }
                    ) {
                        if (pagerState.canScrollForward) {
                            Text("Next")
                        } else {
                            Text("Finish!")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Onboarding(
            onboardingScope = onboardingScope,
            state = pagerState,
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun NsfwOnboarding(dataStore: DataStore) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NsfwSettings(dataStore)
    }
}

@Composable
private fun BehaviorOnboarding(dataStore: DataStore) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        BehaviorSettings(dataStore)
    }
}
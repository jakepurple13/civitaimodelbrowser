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
import androidx.lifecycle.compose.dropUnlessResumed
import com.programmersbox.common.DataStore
import com.programmersbox.common.presentation.onboarding.topics.FinishContent
import com.programmersbox.common.presentation.onboarding.topics.WelcomeContent
import com.programmersbox.common.presentation.settings.BehaviorSettings
import com.programmersbox.common.presentation.settings.NsfwSettings
import com.programmersbox.resources.Res
import com.programmersbox.resources.back
import com.programmersbox.resources.central_app_name
import com.programmersbox.resources.confirm
import com.programmersbox.resources.dismiss
import com.programmersbox.resources.finish
import com.programmersbox.resources.next
import com.programmersbox.resources.skip
import com.programmersbox.resources.skip_onboarding_message
import com.programmersbox.resources.skip_onboarding_title
import com.programmersbox.resources.welcome
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
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
                    title = { Text(stringResource(Res.string.welcome)) },
                    subtitle = { Text(stringResource(Res.string.central_app_name)) },
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
                                    ) { Text(stringResource(Res.string.confirm)) }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { skipOnboarding = false }
                                    ) { Text(stringResource(Res.string.dismiss)) }
                                },
                                title = { Text(stringResource(Res.string.skip_onboarding_title)) },
                                text = { Text(stringResource(Res.string.skip_onboarding_message)) }
                            )
                        }

                        TextButton(
                            onClick = { skipOnboarding = true }
                        ) { Text(stringResource(Res.string.skip)) }
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
                    ) { Text(stringResource(Res.string.back)) }

                    OnboardingIndicator(pagerState)
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = dropUnlessResumed {
                            if (pagerState.canScrollForward) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            } else {
                                scope.launch { hasSeenOnboarding.update(true) }
                                    .invokeOnCompletion { onFinish() }
                            }
                        }
                    ) {
                        if (pagerState.canScrollForward) {
                            Text(stringResource(Res.string.next))
                        } else {
                            Text(stringResource(Res.string.finish))
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
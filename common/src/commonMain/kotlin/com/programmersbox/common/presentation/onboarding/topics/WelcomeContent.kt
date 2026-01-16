package com.programmersbox.common.presentation.onboarding.topics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun WelcomeContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .size(150.dp)
        ) {
            Image(
                painterResource(Res.drawable.civitai_logo),
                null,
                modifier = Modifier.matchParentSize()
            )
        }

        HorizontalDivider()

        // language="Markdown"
        Markdown(
            """
Experience the world of AI art models with a stunning, adaptive interface built for Android, iOS, and Desktop. Whether you are on a phone or tablet, enjoy a fluid Material 3 design featuring dynamic colors and beautiful glassmorphism effects.

**Key Features**:

  - **Explore & Discover**: Browse the latest, top-rated, and most downloaded models from CivitAI with advanced search and filtering.

  - **Curate Your Collection**: Organize your inspiration with Favorites and Custom Lists. Securely backup and restore your data at any time.

  - **Total Control**: Manage your feed with a robust Blacklist and advanced NSFW handling—featuring customizable blur strength and toggle controls.

  - **Insights & Sharing**: Track your browsing stats and easily share models via QR codes.
            """.trimIndent(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
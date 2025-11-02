package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingSuspense(loading: Boolean, content: @Composable () -> Unit) {
    Crossfade(loading) { loading ->
        when {
            loading -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) { ContainedLoadingIndicator(Modifier.scale(2f)) }
            else -> content()
        }
    }
}
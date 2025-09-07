package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingSuspense(loading: Boolean, content: @Composable () -> Unit) {
    Crossfade(loading) { loading ->
        when {
            loading -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) { CircularProgressIndicator(modifier = Modifier.width(64.dp)) }
            else -> content()
        }
    }
}
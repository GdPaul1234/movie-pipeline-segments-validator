package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.AsyncImage

@Composable
fun AsyncImageWithPrevious(url: String) {
    var previousPainter by remember { mutableStateOf<Painter?>(null) }

    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        onSuccess = { previousPainter = it.painter },
        placeholder = previousPainter
    )
}
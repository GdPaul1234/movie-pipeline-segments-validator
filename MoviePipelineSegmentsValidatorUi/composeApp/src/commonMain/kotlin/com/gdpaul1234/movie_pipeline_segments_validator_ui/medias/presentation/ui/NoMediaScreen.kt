package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.Res
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.animated_images_24px
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.select_media_subtitle
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.select_media_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun NoMediaScreen() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painterResource(Res.drawable.animated_images_24px),
                contentDescription = stringResource(Res.string.select_media_title),
                modifier = Modifier.size(72.dp),
            )
            Text(
                stringResource(Res.string.select_media_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(Res.string.select_media_subtitle),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.Res
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.media_validate_segments
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.playlist_add_check_24px
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ValidateSegmentsButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        text = { Text(stringResource(Res.string.media_validate_segments)) },
        icon = { Icon(painterResource(Res.drawable.playlist_add_check_24px), null) },
        onClick = onClick,
    )
}
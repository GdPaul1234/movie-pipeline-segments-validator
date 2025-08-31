package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data

import org.openapitools.client.models.Media
import org.openapitools.client.models.MediaMetadata

data class MediaUiState(
    val loading: Boolean = false,
    val errors: List<String> = listOf(),

    val media: Media? = null,
    val position: Double = 0.0,
    val duration: Double? = null,
    val recordingMetadata: MediaMetadata? = null
)

package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data

import org.openapitools.client.models.Media
import org.openapitools.client.models.MediaMetadata
import org.openapitools.client.models.SegmentOutput

enum class SegmentsView { LIST, TIMELINE }
enum class SegmentsSelectionMode { SINGLE, MULTI }

data class MediaUiState(
    val loading: Boolean = false,
    val errors: List<String> = listOf(),

    val media: Media? = null,
    val importedSegments: Map<String, String> = emptyMap(),
    val recordingMetadata: MediaMetadata? = null,

    val position: Double = 0.0,
    val duration: Double? = null,

    val segmentsView: SegmentsView = SegmentsView.TIMELINE,

    val segmentsSelectionMode: SegmentsSelectionMode = SegmentsSelectionMode.SINGLE,
    val selectedSegments: Set<SegmentOutput> = emptySet()
)

package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data

import org.openapitools.client.models.Media
import org.openapitools.client.models.Session

data class SessionUiState(
    val mediaStateEq: Media.State,
    val loading: Boolean = true,
    val errors: List<String> = listOf(),

    val selectedMediaStem: String = "",
    val session: Session = dummyNewSessionEntry.value,
    val filteredMedias: Map<String, Media> = emptyMap(),
)
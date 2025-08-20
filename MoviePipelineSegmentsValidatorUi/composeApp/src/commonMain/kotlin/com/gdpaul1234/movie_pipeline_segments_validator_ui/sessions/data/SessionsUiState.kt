package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data

import org.openapitools.client.models.Session

data class SessionsUiState(
    val loading: Boolean = false,
    val errors: List<String> = listOf(),
    val selectedSessionEntryKey: String = "",
    val sessions: Set<Map.Entry<String, Session>> = setOf(),
)
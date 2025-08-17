package com.gdpaul1234.movie_pipeline_segments_validator_ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.rememberDataStore
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.RecentSessionsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val dataStore = rememberDataStore()
    val sessionsRepository = SessionsRepository(dataStore)

    MaterialTheme {
        RecentSessionsScreen(sessionsRepository)
    }
}
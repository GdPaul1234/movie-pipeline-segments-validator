package com.gdpaul1234.movie_pipeline_segments_validator_ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.RecentSessionsScreen

@Composable
fun App(dataStore: DataStore<Preferences>) {
    val sessionsRepository = SessionsRepository(dataStore)

    MaterialTheme {
        RecentSessionsScreen(sessionsRepository)
    }
}
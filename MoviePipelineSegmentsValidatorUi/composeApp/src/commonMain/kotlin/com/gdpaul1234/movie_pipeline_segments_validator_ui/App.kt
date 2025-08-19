package com.gdpaul1234.movie_pipeline_segments_validator_ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.RecentSessionsScreen
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.SessionsViewModel

@Composable
fun App(dataStore: DataStore<Preferences>) {
    val extras = MutableCreationExtras().apply {
        val sessionsRepository = SessionsRepository(dataStore)
        set(SessionsViewModel.SESSION_REPOSITORY_KEY, sessionsRepository)
    }

    val viewModel: SessionsViewModel = viewModel(
        factory = SessionsViewModel.Factory,
        extras = extras
    )

    MaterialTheme {
        RecentSessionsScreen(viewModel)
    }
}
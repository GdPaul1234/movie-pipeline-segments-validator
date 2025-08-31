package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation

import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.RecentSessionsScreen
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.SessionsViewModel
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavGraphBuilder.homeDestination(
    sessionsRepository: SessionsRepository,
    navController: NavHostController
) {
    composable<HomeRoute> {
        val extras = MutableCreationExtras().apply { set(SessionsViewModel.SESSION_REPOSITORY_KEY, sessionsRepository) }
        val viewModel: SessionsViewModel = viewModel(factory = SessionsViewModel.Factory, extras = extras)
        RecentSessionsScreen(viewModel, navController)
    }
}

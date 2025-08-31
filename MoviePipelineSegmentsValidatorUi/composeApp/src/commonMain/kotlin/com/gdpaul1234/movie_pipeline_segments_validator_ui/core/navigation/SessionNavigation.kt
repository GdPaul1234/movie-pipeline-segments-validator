package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation

import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.SessionScreen
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui.SessionViewModel
import kotlinx.serialization.Serializable
import org.openapitools.client.models.Media

@Serializable
data class SessionRoute(
    val endpoint: String,
    val sessionId: String,
    val mediaStateEq: String // Media.State
)

fun NavGraphBuilder.sessionDestination(
    sessionsRepository: SessionsRepository,
    navController: NavHostController
) {
    composable<SessionRoute> { backStackEntry ->
        val (endpoint, sessionId, mediaStateEq) = backStackEntry.toRoute() as SessionRoute
        val extras = MutableCreationExtras().apply {
            set(SessionViewModel.ENDPOINT_KEY, endpoint)
            set(SessionViewModel.SESSION_ID_KEY, sessionId)
            set(SessionViewModel.MEDIA_STATE_EQ_KEY, enumValueOf<Media.State>(mediaStateEq))
            set(SessionViewModel.SESSION_REPOSITORY_KEY, sessionsRepository)
        }

        val viewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory, extras = extras)
        SessionScreen(viewModel, navController)
    }
}

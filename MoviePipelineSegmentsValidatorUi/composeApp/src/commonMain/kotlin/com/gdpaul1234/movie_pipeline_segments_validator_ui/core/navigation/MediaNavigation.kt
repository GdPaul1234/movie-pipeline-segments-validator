package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui.MediaViewModel
import kotlinx.serialization.Serializable

@Serializable
data class MediaRoute(
    val endpoint: String,
    val sessionId: String,
    val mediaStem: String
)

@Composable
fun buildMediaViewModel(route: MediaRoute, sessionsRepository: SessionsRepository) : MediaViewModel {
    val (endpoint, sessionId, mediaStem) = route
    return viewModel<MediaViewModel>(
        key = "$sessionId@$endpoint/$mediaStem",
        factory = MediaViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(MediaViewModel.ENDPOINT_KEY, endpoint)
            set(MediaViewModel.SESSION_ID_KEY, sessionId)
            set(MediaViewModel.MEDIA_STEM_KEY, mediaStem)
            set(MediaViewModel.SESSION_REPOSITORY_KEY, sessionsRepository)
        }
    )
}

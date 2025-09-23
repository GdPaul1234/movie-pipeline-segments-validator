package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation.SessionRoute
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network.SessionsService
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui.MediaViewModel
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data.SessionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openapitools.client.models.Media
import kotlin.reflect.KClass

class SessionViewModel(
    val endpoint: String,
    val sessionId: String,
    private val mediaStateEq: Media.State,
    val sessionsRepository: SessionsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SessionUiState(mediaStateEq))
    val uiState = _uiState.asStateFlow()

    private val sessionService = SessionsService(endpoint, sessionsRepository)

    init {
        viewModelScope.launch {
            loadableErrorWrapHandler { sessionService.getSession(sessionId, refresh = false) }

            if (uiState.value.errors.isNotEmpty()) return@launch

            sessionsRepository.get(endpoint, sessionId).collect { session ->
                checkNotNull(session)

                _uiState.update { currentState ->
                    currentState.copy(
                        session = session,
                        filteredMedias = session.medias.filterValues { it.state == mediaStateEq }
                    )
                }
            }
        }
    }

    fun navigateTo(selectedMediaStem: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedMediaStem = selectedMediaStem)
        }
    }

    fun navigateToOtherSessionMediaState(navController: NavHostController, mediaStateEq: Media.State) {
        val route = SessionRoute(endpoint, sessionId, mediaStateEq.value)
        navController.navigate(route)
    }

    private suspend fun <R> loadableErrorWrapHandler (block: suspend () -> R) {
        _uiState.update { currentState -> currentState.copy(loading = true, errors = listOf()) }
        try {
            block()
        } catch (e: Exception) {
            e.message?.let { _uiState.update { currentState -> currentState.copy(errors = currentState.errors + listOf(it)) } }
            e.printStackTrace()
        } finally {
            _uiState.update { currentState -> currentState.copy(loading = false) }
        }
    }

    companion object {
        val ENDPOINT_KEY = object : CreationExtras.Key<String> {}
        val SESSION_ID_KEY = object : CreationExtras.Key<String> {}
        val MEDIA_STATE_EQ_KEY = object : CreationExtras.Key<Media.State> {}
        val SESSION_REPOSITORY_KEY = object : CreationExtras.Key<SessionsRepository> {}

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                val endpoint = checkNotNull(extras[ENDPOINT_KEY])
                val sessionId = checkNotNull(extras[SESSION_ID_KEY])
                val mediaStateEq = checkNotNull(extras[MEDIA_STATE_EQ_KEY])
                val sessionsRepository = checkNotNull(extras[SESSION_REPOSITORY_KEY])
                return SessionViewModel(endpoint, sessionId, mediaStateEq, sessionsRepository) as T
            }
        }
    }
}
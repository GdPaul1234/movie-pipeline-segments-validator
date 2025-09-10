package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavHostController
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation.SessionRoute
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network.SessionsService
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data.SessionsUiState
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data.dummyNewSessionEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openapitools.client.models.Media
import org.openapitools.client.models.Session
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime

class SessionsViewModel(
    private val sessionsRepository: SessionsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SessionsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionsRepository.getRecents().collect {
                _uiState.update { currentState -> currentState.copy(sessions = it) }
            }
        }
    }

    fun navigateTo(selectedSessionEntryKey: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedSessionEntryKey = selectedSessionEntryKey)
        }
    }

    fun createSession(endpoint: String, rootPath: String) =
        viewModelScope.launch {
            loadableErrorWrapHandler {
                val newSession = SessionsService(endpoint, sessionsRepository).createSession(rootPath)
                val newSessionKey = "${newSession.id}@$endpoint"
                _uiState.update { currentState ->  currentState.copy(selectedSessionEntryKey = newSessionKey) }
            }
        }

    fun deleteSession(endpoint: String, session: Session) =
        viewModelScope.launch {
            loadableErrorWrapHandler {
                SessionsService(endpoint, sessionsRepository).deleteSession(session.id)
                _uiState.update { currentState -> currentState.copy(selectedSessionEntryKey = dummyNewSessionEntry.key) }
            }
        }

    fun openSession(navController: NavHostController, endpoint: String, session: Session) {
        val route = SessionRoute(endpoint, session.id, Media.State.waiting_segment_review.value)
        navController.navigate(route)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun loadSession(endpoint: String, sessionId: String): Session {
        val sessionsService = SessionsService(endpoint, sessionsRepository)

        val localSession = sessionsRepository.get(endpoint, sessionId).firstOrNull()
        val remoteSession = sessionsService.getSession(sessionId, refresh = false)

        return when {
            localSession == null || localSession.updatedAt < remoteSession.updatedAt ->
                sessionsService.getSession(sessionId, refresh = true)
            else -> remoteSession
        }
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
        val SESSION_REPOSITORY_KEY = object : CreationExtras.Key<SessionsRepository> {}

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                val sessionsRepository = checkNotNull(extras[SESSION_REPOSITORY_KEY])
                return SessionsViewModel(sessionsRepository) as T
            }
        }
    }
}
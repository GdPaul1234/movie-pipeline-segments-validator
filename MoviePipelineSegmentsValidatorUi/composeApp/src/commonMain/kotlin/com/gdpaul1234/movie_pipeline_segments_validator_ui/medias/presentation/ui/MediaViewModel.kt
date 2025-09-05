package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network.MediasService
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.MediaUiState
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsSelectionMode
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsView
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openapitools.client.models.SegmentOutput
import kotlin.reflect.KClass

class MediaViewModel(
    private val endpoint: String,
    private val sessionId: String,
    private val mediaStem: String,
    private val sessionsRepository: SessionsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState = _uiState.asStateFlow()

    private val mediasService = MediasService(endpoint, sessionId, sessionsRepository)

    init {
        viewModelScope.launch {
            async {
                sessionsRepository.getMedia(endpoint, sessionId, mediaStem).collect {
                    _uiState.update { currentState -> currentState.copy(media = it) }
                }
            }

            async {
                loadableErrorWrapHandler {
                    val mediaDetails = mediasService.getMediaInDetails(mediaStem)

                    _uiState.update { currentState ->
                        currentState.copy(
                            duration = mediaDetails.duration,
                            recordingMetadata = mediaDetails.recordingMetadata
                        )
                    }
                }
            }
        }
    }

    fun setTitle(title: String) {
        _uiState.update { currentState ->
            currentState.copy(media = currentState.media?.copy(title = title))
        }
    }

    fun setSkipBackup(skipBackup: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(media = currentState.media?.copy(skipBackup = skipBackup))
        }
    }

    fun setPosition(position: Number) {
        _uiState.update { currentState -> currentState.copy(position = position.toDouble()) }
    }

    fun toggleSegmentsView() {
        _uiState.update { currentState ->
            currentState.copy(
                segmentsView = when (currentState.segmentsView) {
                    SegmentsView.LIST -> SegmentsView.TIMELINE
                    SegmentsView.TIMELINE -> SegmentsView.LIST
                }
            )
        }
    }

    fun setSelectionMode(multiSelectionEnabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                segmentsSelectionMode = when (multiSelectionEnabled) {
                    true -> SegmentsSelectionMode.MULTI
                    else -> SegmentsSelectionMode.SINGLE
                }
            )
        }
    }

    fun toggleSegment(segment: SegmentOutput) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedSegments = when (currentState.segmentsSelectionMode) {
                    SegmentsSelectionMode.SINGLE -> when {
                        segment in currentState.selectedSegments -> emptySet()
                        else -> setOf(segment)
                    }

                    SegmentsSelectionMode.MULTI -> when {
                        segment in currentState.selectedSegments -> currentState.selectedSegments.filter { it != segment }.toSet()
                        else -> currentState.selectedSegments + setOf(segment)
                    }
                }
            )

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
        val ENDPOINT_KEY = object : CreationExtras.Key<String> {}
        val SESSION_ID_KEY = object : CreationExtras.Key<String> {}
        val MEDIA_STEM_KEY = object : CreationExtras.Key<String> {}
        val SESSION_REPOSITORY_KEY = object : CreationExtras.Key<SessionsRepository> {}

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                val endpoint = checkNotNull(extras[ENDPOINT_KEY])
                val sessionId = checkNotNull(extras[SESSION_ID_KEY])
                val mediaStem = checkNotNull(extras[MEDIA_STEM_KEY])
                val sessionsRepository = checkNotNull(extras[SESSION_REPOSITORY_KEY])
                return MediaViewModel(endpoint, sessionId, mediaStem, sessionsRepository) as T
            }
        }
    }
}
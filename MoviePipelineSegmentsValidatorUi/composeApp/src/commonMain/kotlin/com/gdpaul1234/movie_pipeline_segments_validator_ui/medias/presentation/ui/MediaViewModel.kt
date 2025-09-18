package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network.MediasService
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network.SegmentsService
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.MediaUiState
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsSelectionMode
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openapitools.client.models.*
import java.net.URLEncoder
import java.util.*
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
    private val segmentsService by lazy { SegmentsService(endpoint, sessionId, mediaStem, sessionsRepository) }

    init {
        viewModelScope.launch {
            loadableErrorWrapHandler {
                val mediaDetails = mediasService.getMediaInDetails(mediaStem)

                _uiState.update { currentState ->
                    currentState.copy(
                        media = mediaDetails.media,
                        duration = mediaDetails.duration,
                        recordingMetadata = mediaDetails.recordingMetadata,
                        importedSegments = mediaDetails.importedSegments
                    )
                }
            }
        }
    }

    fun getFrameUrl() =
        listOf(
            "$endpoint/sessions/$sessionId/medias",
            URLEncoder.encode(mediaStem, "UTF-8").replace("+", "%20"),
            "frames/${String.format(Locale.ENGLISH, "%.1f", uiState.value.position)}s"
        ).joinToString("/")

    fun setTitle(title: String) = _uiState.update { currentState -> currentState.copy(media = currentState.media?.copy(title = title)) }
    fun setSkipBackup(skipBackup: Boolean) = _uiState.update { currentState -> currentState.copy(media = currentState.media?.copy(skipBackup = skipBackup)) }

    fun setPosition(position: Number) = _uiState.update { currentState -> currentState.copy(position = position.toDouble()) }
    fun setPositionAtStartOfSelectedSegment() = uiState.value.selectedSegments.single().run { setPosition(start) }
    fun setPositionAtEndOfSelectedSegment() = uiState.value.selectedSegments.single().run { setPosition(end) }


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
                segmentsSelectionMode = when {
                    multiSelectionEnabled -> SegmentsSelectionMode.MULTI
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

    fun addSegmentAtCurrentPosition() = viewModelScope.launch {
        errorWrapHandler {
            segmentsService.addSegment(uiState.value.position).also {
                _uiState.update { currentState -> currentState.copy(media = currentState.media?.copy(segments = it.segments)) }
            }
        }
    }

    fun removeSelectedSegments() = viewModelScope.launch {
        errorWrapHandler {
            segmentsService.removeSegments(SegmentsDeleteBody(uiState.value.selectedSegments.map { (start, end) -> SegmentInput(start, end) })).also {
                _uiState.update { currentState -> currentState.copy(selectedSegments = emptySet(), media = currentState.media?.copy(segments = it.segments)) }
            }
        }
    }

    fun mergeSelectedSegments() = viewModelScope.launch {
        errorWrapHandler {
            segmentsService.mergeSegments(SegmentsMergeBody(uiState.value.selectedSegments.map { (start, end) -> SegmentInput(start, end) })).also {
                _uiState.update { currentState -> currentState.copy(selectedSegments = emptySet(), media = currentState.media?.copy(segments = it.segments)) }
            }
        }
    }

    fun setSelectedSegmentStart() = viewModelScope.launch {
        val (start, end) = uiState.value.selectedSegments.single()
        val body = SegmentEditBody(uiState.value.position, SegmentEditBody.Edge.start)

        errorWrapHandler {
            segmentsService.editSegment(start, end, body).also {
                _uiState.update { currentState -> currentState.copy(selectedSegments = emptySet(), media = currentState.media?.copy(segments = it.segments)) }
            }
        }
    }

    fun setSelectedSegmentEnd() = viewModelScope.launch {
        val (start, end) = uiState.value.selectedSegments.single()
        val body = SegmentEditBody(uiState.value.position, SegmentEditBody.Edge.end)

        errorWrapHandler {
            segmentsService.editSegment(start, end, body).also {
                _uiState.update { currentState -> currentState.copy(selectedSegments = emptySet(), media = currentState.media?.copy(segments = it.segments)) }
            }
        }
    }

    fun validateSegments(navigateTo: ((String) -> Unit)) = viewModelScope.launch {
        thenNavigateToNextMedia(navigateTo) { media ->
            loadableErrorWrapHandler {
                mediasService.validateMediaSegments(
                    mediaStem = mediaStem,
                    body = ValidateSegmentsBody(media.title, media.skipBackup)
                )
            }
        }
    }

    fun importSegments(detectorKey: String) = viewModelScope.launch {
        loadableErrorWrapHandler {
            segmentsService.loadImportedSegments(detectorKey).also {
                _uiState.update { currentState -> currentState.copy(media = currentState.media?.copy(segments = it.segments)) }
            }
        }
    }

    private suspend fun thenNavigateToNextMedia(navigateTo: ((String) -> Unit), block: suspend (Media) -> Unit) {
        val media = checkNotNull(uiState.value.media)
        val session = checkNotNull(sessionsRepository.get(endpoint, sessionId).first())

        val nextMediaKey = session.medias.entries
            .filter { it.value.state == media.state }
            .dropWhile { it.value.filepath <= media.filepath }
            .firstOrNull()
            ?.key

        block(media)

        if (nextMediaKey != null) navigateTo(nextMediaKey)
    }

    private suspend fun <R> loadableErrorWrapHandler(block: suspend () -> R) {
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

    private suspend fun <R> errorWrapHandler(block: suspend () -> R) {
        _uiState.update { currentState -> currentState.copy(errors = listOf()) }
        try {
            block()
        } catch (e: Exception) {
            e.message?.let { _uiState.update { currentState -> currentState.copy(errors = currentState.errors + listOf(it)) } }
            e.printStackTrace()
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
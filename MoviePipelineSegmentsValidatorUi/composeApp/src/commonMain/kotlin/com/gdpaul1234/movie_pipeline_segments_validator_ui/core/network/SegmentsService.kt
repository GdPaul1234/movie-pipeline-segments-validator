package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network

import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import org.openapitools.client.apis.SegmentsApi
import org.openapitools.client.infrastructure.HttpResponse
import org.openapitools.client.models.Media
import org.openapitools.client.models.SegmentCreateBody
import org.openapitools.client.models.SegmentEditBody
import org.openapitools.client.models.SegmentsDeleteBody
import org.openapitools.client.models.SegmentsMergeBody

class SegmentsService(
    private val endpoint: String,
    private val sessionId: String,
    private val mediaStem: String,
    private val sessionsRepository: SessionsRepository
) {
    private val client by lazy {
        SegmentsApi(
            endpoint,
            httpClientConfig = ::setHttpClientConfig
        )
    }

    private suspend fun persistMedia(response: HttpResponse<Media>) =
        response.body().also { sessionsRepository.updateMedia(endpoint, sessionId, it) }

    suspend fun addSegment (position: Double) =
        persistMedia(client.createSegmentSessionsSessionIdMediasMediaStemSegmentsPost(mediaStem, sessionId, SegmentCreateBody(position)))

    suspend fun removeSegments(body: SegmentsDeleteBody) =
        persistMedia(client.deleteSegmentsSessionsSessionIdMediasMediaStemSegmentsDelete(mediaStem, sessionId, body))

    suspend fun editSegment(start: Double, end: Double, body: SegmentEditBody) =
        persistMedia(client.editSegmentSessionsSessionIdMediasMediaStemSegmentsStartSEndSPatch(start, end, mediaStem, sessionId, body))

    suspend fun mergeSegments(body: SegmentsMergeBody) =
        persistMedia(client.mergeSegmentsSessionsSessionIdMediasMediaStemSegmentsMergePost(mediaStem, sessionId, body))

    suspend fun loadImportedSegments(detectorKey: String) =
        persistMedia(client.loadImportedSegmentsSessionsSessionIdMediasMediaStemSegmentsDetectorKeyImportPost(mediaStem, detectorKey, sessionId))
}
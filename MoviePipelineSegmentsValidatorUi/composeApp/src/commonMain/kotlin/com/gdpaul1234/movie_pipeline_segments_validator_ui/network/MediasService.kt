package com.gdpaul1234.movie_pipeline_segments_validator_ui.network

import com.gdpaul1234.movie_pipeline_segments_validator_ui.database.SessionsRepository
import org.openapitools.client.apis.MediasApi
import org.openapitools.client.infrastructure.HttpResponse
import org.openapitools.client.models.MediaOut

class MediasService(
    private val endpoint: String,
    private val sessionId: String,
    private val sessionsRepository: SessionsRepository
) {
    private val client by lazy {
        MediasApi(
            endpoint, httpClientConfig = ::setHttpClientConfig
        )
    }

    private suspend fun persistMedia(response: HttpResponse<MediaOut>) =
        response.body().also { sessionsRepository.updateMedia(endpoint, sessionId, it.media) }

    suspend fun getMediaInDetails(mediaStem: String) =
        persistMedia(client.showMediaSessionsSessionIdMediasMediaStemGet(mediaStem, sessionId))


    suspend fun validateMediaSegments(mediaStem: String) =
        client.validateMediaSegmentsSessionsSessionIdMediasMediaStemValidateSegmentsPost(mediaStem, sessionId).body()
            .also { getMediaInDetails(mediaStem) }
}
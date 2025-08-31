package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network

import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import kotlinx.coroutines.flow.firstOrNull
import org.openapitools.client.apis.SessionsApi
import org.openapitools.client.infrastructure.HttpResponse
import org.openapitools.client.models.Session
import org.openapitools.client.models.SessionCreateBody

class SessionsService(
    private val endpoint: String,
    private val sessionsRepository: SessionsRepository
) {
    private val client by lazy {
        SessionsApi(
            endpoint, httpClientConfig = ::setHttpClientConfig
        )
    }

    private suspend fun persistSession(response: HttpResponse<Session>) =
        response.body().also { sessionsRepository.set(endpoint, it) }

    suspend fun createSession(rootPath: String) =
        persistSession(client.createSessionSessionsPost(SessionCreateBody(rootPath)))

    suspend fun getSession(sessionId: String, reload: Boolean = false): Session = when (reload) {
        false -> sessionsRepository.get(endpoint, sessionId).firstOrNull()
        true -> null
    } ?: persistSession(client.showSessionSessionsSessionIdGet(sessionId))

    suspend fun deleteSession(sessionId: String) {
        client.destroySessionSessionsSessionIdDelete(sessionId)
        sessionsRepository.delete(endpoint, sessionId)
    }
}

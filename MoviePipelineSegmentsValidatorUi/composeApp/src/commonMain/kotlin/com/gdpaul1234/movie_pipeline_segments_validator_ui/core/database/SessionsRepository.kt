package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.serialization.json.Json
import org.openapitools.client.models.Media
import org.openapitools.client.models.Session
import java.io.File
import kotlin.jvm.Throws
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SessionsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private fun getKey(endpoint: String, sessionId: String) = "$sessionId@$endpoint"

    fun getRecents(): Flow<Set<Map.Entry<String, Session>>> {
        return dataStore.data.map { sessions ->
            sessions.asMap()
                .mapKeys { it.key.toString() }
                .mapValues { Json.decodeFromString<Session>(it.value as String) }
                .entries
        }
    }

    fun get(endpoint: String, sessionId: String): Flow<Session?> {
        val sessionKey = stringPreferencesKey(getKey(endpoint, sessionId))
        return dataStore.data.map { sessions ->
            val value = sessions[sessionKey]
            value?.let { Json.decodeFromString<Session>(it) }
        }
    }

    suspend fun set(endpoint: String, session: Session) {
        dataStore.edit { sessions ->
            val sessionKey = stringPreferencesKey(getKey(endpoint, session.id))
            sessions[sessionKey] = Json.encodeToString(session)
        }
    }

    @OptIn(ExperimentalTime::class)
    @Throws(NoSuchElementException::class)
    suspend fun updateMedia(endpoint: String, sessionId: String, media: Media) {
        val session = checkNotNull(get(endpoint, sessionId).single())

        dataStore.edit { sessions ->
            val updatedSession = session.copy(
                updatedAt = Clock.System.now(), medias = session.medias.toMutableMap().apply {
                    this[File(media.filepath).nameWithoutExtension] = media
                })

            val sessionKey = stringPreferencesKey(getKey(endpoint, session.id))
            sessions[sessionKey] = Json.encodeToString(updatedSession)
        }
    }

    suspend fun delete(endpoint: String, sessionId: String) {
        dataStore.edit {
            val sessionKey = stringPreferencesKey(getKey(endpoint, sessionId))
            it.remove(sessionKey)
        }
    }
}

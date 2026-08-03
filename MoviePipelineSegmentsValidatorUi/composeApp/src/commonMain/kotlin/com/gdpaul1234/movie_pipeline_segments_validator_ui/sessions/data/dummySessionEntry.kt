package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data

import org.openapitools.client.models.Session
import kotlin.time.Clock

val dummyNewSessionEntry = mapOf(
    "new_session" to Session(
        id = "new_session",
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
        rootPath = "null",
        medias = emptyMap()
    )
).entries.first()

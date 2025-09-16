package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.util

import org.openapitools.client.models.Media
import org.openapitools.client.models.Session

fun getMediaStem(media: Media, session: Session) =
    media.filepath
        .substringAfter(session.rootPath)
        .removePrefix("/")
        .removePrefix("\\")
        .substringBeforeLast(".")

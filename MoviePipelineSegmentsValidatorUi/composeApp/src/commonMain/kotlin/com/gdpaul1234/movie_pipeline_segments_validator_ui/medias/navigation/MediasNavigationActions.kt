package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.navigation

import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.openapitools.client.models.Media


data class MediasTopLevelDestination(
    val mediaStateEq: Media.State,
    val icon: DrawableResource,
    val iconTextResource: StringResource
)

val TOP_LEVEL_DESTINATIONS = listOf(
    MediasTopLevelDestination(
        mediaStateEq = Media.State.waiting_metadata,
        icon = Res.drawable.upload_24px,
        iconTextResource = Res.string.stats_waiting_metadata,
    ),
    MediasTopLevelDestination(
        mediaStateEq = Media.State.no_segment,
        icon = Res.drawable.playlist_remove_24px,
        iconTextResource = Res.string.stats_no_segment,
    ),
    MediasTopLevelDestination(
        mediaStateEq = Media.State.waiting_segment_review,
        icon = Res.drawable.pending_actions_24px,
        iconTextResource = Res.string.stats_waiting_segment_review,
    ),
    MediasTopLevelDestination(
        mediaStateEq = Media.State.segment_reviewed,
        icon = Res.drawable.task_alt_24px,
        iconTextResource = Res.string.stats_segment_reviewed,
    ),
    MediasTopLevelDestination(
        mediaStateEq = Media.State.media_processing,
        icon = Res.drawable.settings_cinematic_blur_24px,
        iconTextResource = Res.string.stats_media_processing,
    ),
    MediasTopLevelDestination(
        mediaStateEq = Media.State.media_processed,
        icon = Res.drawable.done_all_24px,
        iconTextResource = Res.string.stats_media_processed,
    ),
)

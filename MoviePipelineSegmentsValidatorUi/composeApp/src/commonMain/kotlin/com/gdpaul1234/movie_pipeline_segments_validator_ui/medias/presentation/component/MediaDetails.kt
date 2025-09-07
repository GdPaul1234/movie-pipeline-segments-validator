package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.navigation.TOP_LEVEL_DESTINATIONS
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui.MediaViewModel
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.openapitools.client.models.Media
import org.openapitools.client.models.MediaMetadata
import org.openapitools.client.models.SegmentOutput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetails(
    viewModel: MediaViewModel,
    close: (() -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.media?.let { media ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text =stringResource(Res.string.media_details),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        if (close != null) {
                            IconButton(onClick = { close() }) {
                                Icon(
                                    painterResource(Res.drawable.close_24px),
                                    contentDescription = "Close"
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .consumeWindowInsets(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { InfoSection(media) }
                item { RecordingMetadataSection(uiState.recordingMetadata) }
            }
        }
    }
}

@Composable
@Preview
private fun InfoSection(
    @PreviewParameter(MediaPreviewParameter::class) media: Media,
    isPreview: Boolean = LocalInspectionMode.current
) {
    val listItemColors = ListItemDefaults.colors(CardDefaults.cardColors().containerColor)

    Card(modifier = Modifier.padding(bottom = 16.dp)) {
        ListItem(
            colors = listItemColors,
            headlineContent = { Text(stringResource(Res.string.media_filepath)) },
            supportingContent = { Text(media.filepath) }
        )

        val stateLabel = when {
            isPreview -> media.state.value
            else -> stringResource(Res.allStringResources["stats_${media.state.value}"]!!)
        }

        ListItem(
            colors = listItemColors,
            headlineContent = { Text(stringResource(Res.string.media_state)) },
            supportingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(TOP_LEVEL_DESTINATIONS.find { it.mediaStateEq == media.state }!!.icon),
                        contentDescription = null
                    )
                    Text(stateLabel)
                }
            }
        )

        ListItem(
            colors = listItemColors,
            headlineContent = { Text(stringResource(Res.string.media_imported_segments)) },
            supportingContent = {
                Text(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    text = media.importedSegments.entries.joinToString("\n") { "${it.key}: ${it.value}" },
                    softWrap = false
                )
            }
        )
    }
}

@Composable
@Preview
private fun RecordingMetadataSection(
    @PreviewParameter(RecordingMetadataPreviewParameterProvider::class) recordingMetadata: MediaMetadata?,
) {
    val listItemColors = ListItemDefaults.colors(CardDefaults.cardColors().containerColor)

    Card(modifier = Modifier.padding(bottom = 16.dp)) {
        if (recordingMetadata == null) {
            ListItem(
                colors = listItemColors,
                headlineContent = { Text(stringResource(Res.string.media_no_recording_metadata)) }
            )
        } else {
            val entries = listOf(
                stringResource(Res.string.media_recording_recordingId) to recordingMetadata.recordingId,
                stringResource(Res.string.media_recording_title) to recordingMetadata.title,
                stringResource(Res.string.media_recording_subTitle) to recordingMetadata.subTitle.ifEmpty { "N/A" },
                stringResource(Res.string.media_recording_description) to recordingMetadata.description
            )

            entries.forEach { (label, value) ->
                ListItem(
                    colors = listItemColors,
                    headlineContent = { Text(label) },
                    supportingContent = { Text(value) }
                )
            }
        }
    }
}

class MediaPreviewParameter : PreviewParameterProvider<Media> {
    override val values = sequenceOf(
        Media(
            filepath = "V:\\PVR\\Channel 1_Movie Name_2022-12-05-2203-20.ts",
            state = Media.State.waiting_segment_review,
            title = "Movie Name, le titre long.mp4",
            skipBackup = false,
            importedSegments = mapOf(
                "auto" to "00:00:00.000-01:05:54.840,00:42:38.980-01:49:59.300,01:05:54.840-01:49:59.300",
                "result_2024-10-05T11:40:39.732479" to "00:25:26.000-00:34:06.000,00:40:10.000-01:01:23.000,01:07:34.000-01:17:59.000"
            ),
            segments = listOf(
                SegmentOutput(start = 1526.0, end = 3246.0, duration = 1720.0)
            )
        )
    )
}

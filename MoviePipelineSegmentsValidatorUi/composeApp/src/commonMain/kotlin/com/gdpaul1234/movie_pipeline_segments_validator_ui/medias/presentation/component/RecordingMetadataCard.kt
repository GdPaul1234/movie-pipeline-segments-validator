package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.openapitools.client.models.MediaMetadata
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private data class MetadataField (val fieldName: String, val value: String, val weight: Float, val minWidth: Dp? = null)

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Preview
@Composable
fun MediaRecordingMetadataCard(
    @PreviewParameter(RecordingMetadataPreviewParameterProvider::class) recordingMetadata: MediaMetadata,
    duration: Double?,
    navigateToDetails: (() -> Unit)?
) {
    recordingMetadata.apply {
        Card(modifier = Modifier.padding(bottom = 16.dp)) {
            val listItemColors = ListItemDefaults.colors(CardDefaults.cardColors().containerColor)

            val metadata = listOf(
                MetadataField(stringResource(Res.string.media_channel), channel, 1f),
                MetadataField(stringResource(Res.string.media_start_real), Instant.fromEpochSeconds(startReal).toString(), 1.5f, 256.dp),
                MetadataField(stringResource(Res.string.media_stop_real), Instant.fromEpochSeconds(stopReal).toString(), 1.5f, 256.dp),
                MetadataField(stringResource(Res.string.media_duration), duration?.seconds?.inWholeSeconds?.seconds?.toString() ?: "N/A", 1f, 170.dp)
            )

            FlowRow(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                metadata.forEach { (text, value, weight, minWidth) ->
                    ListItem(
                        modifier = Modifier
                            .weight(weight)
                            .then(minWidth?.let { Modifier.widthIn(it) } ?: Modifier),
                        colors = listItemColors,
                        headlineContent = {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        supportingContent = { Text(text) }
                    )
                }

                ListItem(
                    modifier = Modifier.weight(1f),
                    colors = listItemColors,
                    headlineContent = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(errorMessage) } },
                            state = rememberTooltipState()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "$nbDataErrors",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Icon(
                                    painterResource(Res.drawable.info_24px),
                                    contentDescription = "Info",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    supportingContent = { Text(stringResource(Res.string.media_nb_data_errors)) }
                )


                if (navigateToDetails != null) {
                    ListItem(
                        modifier = Modifier.weight(1f).clickable { navigateToDetails() },
                        colors = listItemColors,
                        headlineContent = {
                            Text(
                                text = stringResource(Res.string.media_more_details_label),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    )
                }
            }
        }
    }
}

class RecordingMetadataPreviewParameterProvider : PreviewParameterProvider<MediaMetadata> {
    override val values = sequenceOf(
        MediaMetadata(
            basename = "Arte_Dark Shadows (VM)_2025-08-1721-01.ts",
            channel = "Arte",
            title = "Arte",
            subTitle = "",
            description = "Film de Tim Burton (Royaume-Uni, 2012, 1h47mn) Un vampire pi\u00e9g\u00e9 sous terre est exhum\u00e9 par erreur en 1972, dans une bourgade de p\u00eacheurs... Une parodie horrifique de Tim Burton, avec la complicit\u00e9 de Johnny Depp, Eva Green, Michelle Pfeiffer, Helena Bonham Carter et Chlo\u00eb Grace Moretz.\n\nAUDIO 1 : FRAN\u00c7AIS - AUDIO 3 : VERSION ORIGINALE - VM (sous-titres optionnels) - AUDIO 4 : AUDIOVISION\nSous-titres pour sourds et malentendants disponibles pour ce programme",
            startReal = 1755455480,
            stopReal = 1755465281,
            errorMessage = "OK",
            nbDataErrors = 2,
            recordingId = "41310a560fea272ff1e916a8567446aa"
        )
    )
}

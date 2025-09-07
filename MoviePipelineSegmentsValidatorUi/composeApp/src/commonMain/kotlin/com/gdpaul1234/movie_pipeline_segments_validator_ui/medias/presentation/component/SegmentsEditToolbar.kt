package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.openapitools.client.models.SegmentOutput

private data class IconButtonData(val label: String, val icon: DrawableResource, val disabled: Boolean, val onClick: () -> Unit)

@Composable
private fun getIconButtons(selectedSegments: Set<SegmentOutput>) : List<IconButtonData> {
    return listOf(
        IconButtonData(
            label = stringResource(Res.string.segment_add),
            icon = Res.drawable.playlist_add_24px,
            disabled = selectedSegments.isNotEmpty(),
            onClick = {/* TODO add segment */}
        ),
        IconButtonData(
            label = pluralStringResource(Res.plurals.segments_remove, selectedSegments.size),
            icon = Res.drawable.playlist_remove_24px,
            disabled = selectedSegments.isEmpty(),
            onClick = {/* TODO Remove segments */}
        ),
        IconButtonData(
            label = stringResource(Res.string.segments_merge),
            icon = Res.drawable.mediation_24px,
            disabled = selectedSegments.size < 2,
            onClick = {/* TODO Merge segments */}
        ),
        /* --- Divider(after index 2) --- */
        IconButtonData(
            label = stringResource(Res.string.go_to_selected_segment_start),
            icon = Res.drawable.skip_next_24px,
            disabled = selectedSegments.size != 1,
            onClick = {/* TODO Go to start of selected segment */}
        ),
        IconButtonData(
            label = stringResource(Res.string.go_to_selected_segment_end),
            icon = Res.drawable.skip_previous_24px,
            disabled = selectedSegments.size != 1,
            onClick = {/* TODO Go to end of selected segment */}
        ),
        /* --- Divider(after index 4) --- */
        IconButtonData(
            label = stringResource(Res.string.split_selected_segment),
            icon = Res.drawable.split_scene_24px,
            disabled = selectedSegments.size != 1,
            onClick = {/* TODO Split segment at position */}
        ),
        IconButtonData(
            label = stringResource(Res.string.set_selected_segment_start),
            icon = Res.drawable.split_scene_right_24px,
            disabled = selectedSegments.size != 1,
            onClick = {/* TODO Set start of selected segment */}
        ),
        IconButtonData(
            label = stringResource(Res.string.set_selected_segment_end),
            icon = Res.drawable.split_scene_left_24px,
            disabled = selectedSegments.size != 1,
            onClick = {/* TODO Set end of selected segment */}
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentsEditVerticalToolbar(
    modifier: Modifier,
    selectedSegments: Set<SegmentOutput>
) {
    val iconButtons = getIconButtons(selectedSegments)

    Surface(
        modifier = modifier.width(IntrinsicSize.Min).clip(CircleShape),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            iconButtons.forEachIndexed { index, (label, icon, disabled, onClick) ->
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(label) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = onClick, enabled = !disabled) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = label
                        )
                    }
                }

                if (index in listOf(2, 4)) {
                    HorizontalDivider(Modifier.width(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentsEditHorizontalToolbar(
    selectedSegments: Set<SegmentOutput>,
    isSmallScreen: Boolean
) {
    val iconButtons = getIconButtons(selectedSegments)
    val filteredIconButtons = remember(isSmallScreen, iconButtons) {
        iconButtons.filter { !isSmallScreen || !it.disabled }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        filteredIconButtons.forEachIndexed { index, (label, icon, disabled, onClick) ->
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(label) } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = onClick, enabled = !disabled) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = label
                    )
                }
            }

            if (!isSmallScreen && index in listOf(2, 4)) {
                VerticalDivider(Modifier.height(32.dp))
            }
        }
    }
}

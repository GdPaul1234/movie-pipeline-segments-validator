package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsSelectionMode
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsView
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaActionsBottomBar(
    segmentsView: SegmentsView,
    segmentsSelectionMode: SegmentsSelectionMode,
    toggleSegmentsView: () -> Unit,
    setSelectionMode: (Boolean) -> Unit,
    importSegments: () -> Unit,
    validateSegments: () -> Unit,
    isReadOnly: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomAppBar(
            modifier = Modifier.widthIn(max = WIDTH_DP_LARGE_LOWER_BOUND.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            // Actions in the left part

            val (selectionModeDescription, selectionModeIcon) = when (segmentsView) {
                SegmentsView.LIST -> Res.string.list_segments_view to Res.drawable.view_list_24px
                SegmentsView.TIMELINE -> Res.string.timeline_segments_view to Res.drawable.view_obj_track_24px
            }

            val spacingBetweenTooltipAndAnchor = 4.dp

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above, spacingBetweenTooltipAndAnchor),
                tooltip = { PlainTooltip { Text(stringResource(selectionModeDescription)) } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = toggleSegmentsView) {
                    Icon(
                        painter = painterResource(selectionModeIcon),
                        contentDescription = stringResource(selectionModeDescription)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above, spacingBetweenTooltipAndAnchor),
                tooltip = { PlainTooltip { Text(stringResource(Res.string.segments_import)) } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = importSegments, enabled = !isReadOnly) {
                    Icon(
                        painter = painterResource(Res.drawable.upload_24px),
                        contentDescription = stringResource(Res.string.segments_import)
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above, spacingBetweenTooltipAndAnchor),
                tooltip = { PlainTooltip { Text(stringResource(Res.string.segments_multi_selection_mode)) } },
                state = rememberTooltipState()
            ) {
                Checkbox(
                    checked = segmentsSelectionMode == SegmentsSelectionMode.MULTI,
                    onCheckedChange = setSelectionMode,
                    enabled = !isReadOnly
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isReadOnly) {
                // Actions in the right part
                Spacer(modifier = Modifier.width(16.dp))
                ValidateSegmentsButton(onClick = validateSegments)
            }
        }
    }
}
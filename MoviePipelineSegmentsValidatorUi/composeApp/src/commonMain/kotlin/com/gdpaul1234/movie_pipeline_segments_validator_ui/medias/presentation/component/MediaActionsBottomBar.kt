package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
) {
    BottomAppBar(contentPadding = PaddingValues(horizontal = 24.dp)) {
        // Actions in the left part

        val (selectionModeDescription, selectionModeIcon) = when (segmentsView) {
            SegmentsView.LIST -> Res.string.list_segments_view to Res.drawable.view_list_24px
            SegmentsView.TIMELINE -> Res.string.timeline_segments_view to Res.drawable.view_obj_track_24px
        }

        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
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
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(stringResource(Res.string.segments_import)) } },
            state = rememberTooltipState()
        ) {
            IconButton(onClick = importSegments) {
                Icon(
                    painter = painterResource(Res.drawable.upload_24px),
                    contentDescription = stringResource(Res.string.segments_import)
                )
            }
        }

        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(stringResource(Res.string.segments_multi_selection_mode)) } },
            state = rememberTooltipState()
        ) {
            Checkbox(
                checked = segmentsSelectionMode == SegmentsSelectionMode.MULTI,
                onCheckedChange = setSelectionMode
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions in the right part

        Spacer(modifier = Modifier.width(16.dp))
        ValidateSegmentsButton(onClick = { /* TODO validate segments */ })
    }
}
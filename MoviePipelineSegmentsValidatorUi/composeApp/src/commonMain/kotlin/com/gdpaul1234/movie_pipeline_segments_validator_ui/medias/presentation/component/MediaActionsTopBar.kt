package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsSelectionMode
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsView
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaActionsTopAppBar(
    segmentsView: SegmentsView,
    segmentsSelectionMode: SegmentsSelectionMode,
    toggleSegmentsView: () -> Unit,
    setSelectionMode: (Boolean) -> Unit,
    importSegments: () -> Unit,
) {
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

    val (selectionModeDescription, selectionModeIcon) = when (segmentsView) {
        SegmentsView.LIST -> Res.string.list_segments_view to Res.drawable.view_list_24px
        SegmentsView.TIMELINE -> Res.string.timeline_segments_view to Res.drawable.view_obj_track_24px
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(stringResource(Res.string.segments_import)) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = importSegments) {
            Icon(
                painter = painterResource(Res.drawable.download_24px),
                contentDescription = stringResource(Res.string.segments_import)
            )
        }
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
}
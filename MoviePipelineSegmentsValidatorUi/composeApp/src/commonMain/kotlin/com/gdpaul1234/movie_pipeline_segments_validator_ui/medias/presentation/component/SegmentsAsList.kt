package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.util.formatSecondsToPeriod
import org.openapitools.client.models.SegmentOutput
import kotlin.time.Duration.Companion.seconds

@Composable
fun SegmentsAsList(
    modifier: Modifier = Modifier,
    segments: List<SegmentOutput>,
    selectedSegments: Set<SegmentOutput>,
    toggleSegment: (SegmentOutput) -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        segments.forEach { segment ->
            val (start, end, duration) = segment
            val label = listOf(
                listOf(start, end).joinToString(" - ") { formatSecondsToPeriod(it) },
                "(${duration.seconds.inWholeSeconds.seconds})"
            ).joinToString(" ")

            InputChip(
                selected = segment in selectedSegments,
                onClick = { toggleSegment(segment) },
                label = { Text(label)},
            )
        }
    }
}
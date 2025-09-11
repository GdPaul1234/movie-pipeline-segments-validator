package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openapitools.client.models.SegmentOutput

private data class SegmentRelativeRowLayout(val item: SegmentOutput, val paddingRight: Dp)

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun SegmentsAsTimeline(
    segments: List<SegmentOutput>,
    selectedSegments: Set<SegmentOutput>,
    position: Double, // current position in seconds
    duration: Double, // total duration in seconds
    toggleSegment: (SegmentOutput) -> Unit
) {
    val colors = SliderDefaults.colors()

    // Render segments with their respective positions and widths
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(MaterialTheme.shapes.small)
            .background(colors.inactiveTrackColor)
    ) {
        val segmentsLayout = remember(segments) {
            segments.zipWithNext { a, b ->
                val delta = b.start - a.end
                val deltaWidth = (delta / duration).coerceIn(0.0, 1.0)

                SegmentRelativeRowLayout(item = a, paddingRight = (deltaWidth * constraints.maxWidth).dp)
            }
        }

        @Composable
        fun SegmentBox(modifier: Modifier, segment: SegmentOutput) {
            // Calculate the relative start width of the segment, ensure values are between 0 and 1
            val segmentWidth = (segment.duration / duration).coerceIn(0.0, 1.0)

            Box(
                modifier = modifier
                    .width((segmentWidth * constraints.maxWidth).dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(
                        // Change background color based on selection state
                        color = if (segment in selectedSegments) colors.activeTrackColor
                        else colors.disabledActiveTrackColor
                    )
                    .clickable { toggleSegment(segment) }
            )
        }

        // Render les segments
        LazyRow(
            modifier = Modifier.size(maxWidth, maxHeight),
            userScrollEnabled = false
        ) {
            if (segments.size == 1) {
                item {
                    val segment = segments.first()

                    // Calculate the relative start position of the segment, ensure values are between 0 and 1
                    val segmentStart = (segment.start / duration).coerceIn(0.0, 1.0)

                    SegmentBox(Modifier.animateItem().offset(x = (segmentStart * constraints.maxWidth).dp), segment)
                }

            } else {
                items(
                    items = segmentsLayout,
                    key = { with(it.item) { "$start-$end" } }
                ) { (segment, paddingRight) -> SegmentBox(Modifier.animateItem().padding(end = paddingRight), segment) }

                item { segments.lastOrNull()?.let { SegmentBox(Modifier.animateItem(), it) } }
            }
        }

        // Render current position indicator
        val positionOffset = (position / duration).coerceIn(0.0, 1.0)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .offset(x = (positionOffset * constraints.maxWidth).dp)
                .background(Color.Red)
        )
    }
}

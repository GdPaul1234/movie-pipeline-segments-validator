package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.openapitools.client.models.SegmentOutput

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
            .clip(ShapeDefaults.Small)
            .background(colors.inactiveTrackColor)
    ) {

        // Render les segments
        segments.forEach { segment ->
            // Calculate the relative start position and width of the segment
            // Ensure values are between 0 and 1
            val segmentStart = (segment.start / duration).coerceIn(0.0, 1.0)
            val segmentWidth = (segment.duration / duration).coerceIn(0.0, 1.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth(segmentWidth.toFloat())
                    .fillMaxHeight()
                    .offset(x = (segmentStart * constraints.maxWidth).dp)
                    .clip(ShapeDefaults.ExtraSmall)
                    .background(
                        // Change background color based on selection state
                        color = if (segment in selectedSegments) colors.activeTrackColor
                        else colors.disabledActiveTrackColor
                    )
                    .clickable { toggleSegment(segment) }
            )
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

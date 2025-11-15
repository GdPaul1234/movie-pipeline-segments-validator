package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import org.openapitools.client.models.SegmentOutput

private data class SegmentRelativeRowLayout(val item: SegmentOutput, val fractionalPaddingRight: Float)

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
    val minSegmentWidth = 4.dp

    // Render segments with their respective positions and widths
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(MaterialTheme.shapes.small)
            .background(colors.inactiveTrackColor)
    ) {
        fun fractionToWidth(fraction: Float) = maxWidth * fraction

        val segmentsLayout = remember(segments) {
            segments.zipWithNext { a, b ->
                val delta = b.start - a.end
                val deltaWidth = (delta / duration).coerceIn(0.0, 1.0).toFloat()
                SegmentRelativeRowLayout(item = a, fractionalPaddingRight = deltaWidth)
            }
        }

        @Composable
        fun SegmentBox(modifier: Modifier, segment: SegmentOutput) {
            // Calculate the relative start width of the segment, ensure values are between 0 and 1
            val segmentWidth = (segment.duration / duration).coerceIn(0.0, 1.0).toFloat()

            Box(
                modifier = modifier
                    .animateContentSize()
                    .size(fractionToWidth(segmentWidth).coerceAtLeast(minSegmentWidth), maxHeight)
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
        Row(Modifier.size(maxWidth, maxHeight)) {
            if (segments.size == 1) {
                val segment = segments.first()

                // Calculate the relative start position of the segment, ensure values are between 0 and 1
                val segmentStart = fractionToWidth((segment.start / duration).coerceIn(0.0, 1.0).toFloat())
                SegmentBox(Modifier.padding(start = segmentStart), segment)

            } else if (segments.size > 1) {
                segmentsLayout.forEachIndexed { index, (segment, paddingRight) ->
                    val paddingLeft = when {
                        index == 0 -> fractionToWidth((segment.start / duration).coerceIn(0.0, 1.0).toFloat())
                        else -> 0.dp
                    }

                    val segmentWidth = fractionToWidth((segment.duration / duration).coerceIn(0.0, 1.0).toFloat())
                    val paddingRight = fractionToWidth(paddingRight) - (if (segmentWidth < minSegmentWidth) minSegmentWidth else 0.dp)

                    SegmentBox(Modifier.padding(start = paddingLeft, end = paddingRight.coerceIn(0.dp, null)), segment)
                }

                SegmentBox(Modifier, segments.last())
            }
        }

        // Render current position indicator
        val positionOffset = fractionToWidth((position / duration).coerceIn(0.0, 1.0).toFloat())
        Box(Modifier.size(2.dp, maxHeight).offset(x = positionOffset).background(Color.Red))
    }
}

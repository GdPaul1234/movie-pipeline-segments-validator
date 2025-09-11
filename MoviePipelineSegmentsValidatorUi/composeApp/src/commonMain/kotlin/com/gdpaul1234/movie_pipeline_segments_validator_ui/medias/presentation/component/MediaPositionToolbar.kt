package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.util.formatSecondsToPeriod
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPositionToolbar(
    modifier: Modifier = Modifier,
    position: Double,
    duration: Double,
    setPosition: (Double) -> Unit,
    isSmallScreen: Boolean = false
) {
    val tonalElevation = if (isSmallScreen) 0.dp else 1.dp

    Row(
        modifier = modifier
            .then(if (isSmallScreen) Modifier else Modifier.padding(8.dp))
            .height(IntrinsicSize.Min)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = tonalElevation
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSecondsToPeriod(position),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = tonalElevation) {
            val adaptiveInfo = currentWindowAdaptiveInfo()
            val options = listOf(1.0, 5.0, 10.0, 30.0).takeWhile { value ->
                value <= when {
                    adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> Double.MAX_VALUE
                    adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> 10.0
                    else -> 5.0
                }
            }

            var selectedIndex by remember { mutableIntStateOf(1) }
            val delta = remember(selectedIndex) { options[selectedIndex] }

            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                SingleChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, value ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            onClick = { selectedIndex = index },
                            selected = index == selectedIndex,
                        ) {
                            Text(value.seconds.toString())
                        }
                    }
                }

                // Replay button

                val replayContentDescription = stringResource(Res.string.replay_x_seconds, delta)

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(replayContentDescription) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { setPosition(max(0.0, position - delta)) }) {
                        Icon(
                            painter = painterResource(
                                when (delta) {
                                    30.0 -> Res.drawable.replay_30_24px
                                    10.0 -> Res.drawable.replay_10_24px
                                    5.0 -> Res.drawable.replay_5_24px
                                    else -> Res.drawable.replay_24px
                                }
                            ),
                            contentDescription = replayContentDescription
                        )
                    }
                }

                // Forward button

                val forwardContentDescription = stringResource(Res.string.forward_x_seconds, delta)

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(forwardContentDescription) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { setPosition(min(position + delta, duration)) }) {
                        Icon(
                            painter = painterResource(
                                when (delta) {
                                    30.0 -> Res.drawable.forward_30_24px
                                    10.0 -> Res.drawable.forward_10_24px
                                    5.0 -> Res.drawable.forward_5_24px
                                    else -> Res.drawable.forward_media_24px
                                }
                            ),
                            contentDescription = forwardContentDescription
                        )
                    }
                }
            }
        }
    }
}
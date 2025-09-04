package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.util

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

fun formatSecondsToPeriod(seconds: Double) =
    seconds.seconds.toComponents { days, hours, minutes, seconds, _ ->
        listOf(minutes, seconds).joinToString(
            separator = ":",
            prefix = "${days.days.inWholeHours + hours}:",
        ) { if (it < 10) "0$it" else it.toString() }
    }
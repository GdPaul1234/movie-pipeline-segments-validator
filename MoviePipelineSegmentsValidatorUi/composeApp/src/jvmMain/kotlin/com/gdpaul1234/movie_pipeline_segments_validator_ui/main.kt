package com.gdpaul1234.movie_pipeline_segments_validator_ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MoviePipelineSegmentsValidatorUi",
    ) {
        App()
    }
}
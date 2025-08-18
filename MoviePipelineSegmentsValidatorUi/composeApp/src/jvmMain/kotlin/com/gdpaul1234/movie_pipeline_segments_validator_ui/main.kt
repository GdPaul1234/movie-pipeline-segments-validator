package com.gdpaul1234.movie_pipeline_segments_validator_ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.createDataStore

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MoviePipelineSegmentsValidatorUi",
    ) {
        App(dataStore = createDataStore())
    }
}
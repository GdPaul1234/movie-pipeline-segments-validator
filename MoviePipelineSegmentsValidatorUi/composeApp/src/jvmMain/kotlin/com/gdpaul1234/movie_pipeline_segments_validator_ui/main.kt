package com.gdpaul1234.movie_pipeline_segments_validator_ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.createDataStore

fun main() = application {
    val state = rememberWindowState(size = DpSize(1280.dp, 800.dp))

    Window(
        onCloseRequest = ::exitApplication,
        state = state,
        title = "MoviePipelineSegmentsValidatorUi",
    ) {
        App(dataStore = createDataStore())
    }
}
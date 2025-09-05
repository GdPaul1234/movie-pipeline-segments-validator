package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

@Composable
fun SegmentsEditToolbar(

) {
    val step by remember { mutableIntStateOf(5) }

    Surface(tonalElevation = 1.dp) {
        Row {
        }
    }
}
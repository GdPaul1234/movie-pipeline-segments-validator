package com.gdpaul1234.movie_pipeline_segments_validator_ui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
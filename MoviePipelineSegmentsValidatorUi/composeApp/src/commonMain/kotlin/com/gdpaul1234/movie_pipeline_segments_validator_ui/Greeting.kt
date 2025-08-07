package com.gdpaul1234.movie_pipeline_segments_validator_ui

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
package com.gdpaul1234.movie_pipeline_segments_validator_ui.network

import io.ktor.client.HttpClientConfig

internal fun setHttpClientConfig(config: HttpClientConfig<*>) {
    config.apply {
        expectSuccess = true
    }
}

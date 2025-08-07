package com.gdpaul1234.movie_pipeline_segments_validator_ui

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()
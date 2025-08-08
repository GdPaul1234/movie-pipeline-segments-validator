package com.gdpaul1234.movie_pipeline_segments_validator_ui.database.createDataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gdpaul1234.movie_pipeline_segments_validator_ui.database.createDataStore
import com.gdpaul1234.movie_pipeline_segments_validator_ui.database.dataStoreFileName
import java.io.File

fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
        file.absolutePath
    }
)

package com.gdpaul1234.movie_pipeline_segments_validator_ui.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.createDataStore
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.dataStoreFileName

fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)

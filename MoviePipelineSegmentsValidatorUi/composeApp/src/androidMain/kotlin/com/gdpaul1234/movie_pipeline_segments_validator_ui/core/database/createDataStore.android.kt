package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)

@Composable
actual fun rememberDataStore(): DataStore<Preferences> {
    val context = LocalContext.current

    return remember {
        createDataStore(context)
    }
}
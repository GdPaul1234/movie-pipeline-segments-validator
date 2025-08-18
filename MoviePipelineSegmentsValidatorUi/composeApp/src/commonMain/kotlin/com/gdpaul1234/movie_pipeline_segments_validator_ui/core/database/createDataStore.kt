package com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import okio.Path.Companion.toPath

@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject() // Used for thread safety
private lateinit var dataStore: DataStore<Preferences> // Late-initialized variable


/**
 *   Gets the singleton DataStore instance, creating it if necessary.
 */
@OptIn(InternalCoroutinesApi::class)
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    synchronized(lock) {
        if (::dataStore.isInitialized) {
            dataStore
        } else {
            PreferenceDataStoreFactory
                .createWithPath(produceFile = { producePath().toPath() })
                .also { dataStore = it }
        }
    }

internal const val dataStoreFileName = "movie_pipeline_segments_validator_sessions.preferences_pb"

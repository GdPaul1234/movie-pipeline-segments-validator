package com.gdpaul1234.movie_pipeline_segments_validator_ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation.HomeRoute
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation.homeDestination
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.navigation.sessionDestination

@Composable
fun App(
    dataStore: DataStore<Preferences>,
    navController: NavHostController = rememberNavController()
) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .build()
    }

    MaterialTheme {
        Scaffold { paddingValues ->
            val sessionsRepository = SessionsRepository(dataStore)

            NavHost(
                navController = navController,
                startDestination = HomeRoute,
                modifier = Modifier.fillMaxSize().padding(paddingValues).consumeWindowInsets(paddingValues)
            ) {
                homeDestination(sessionsRepository, navController)
                sessionDestination(sessionsRepository, navController)
            }
        }
    }
}
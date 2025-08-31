package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.navigation.MediasNavigationWrapper
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui.MediaScreen
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui.NoMediaScreen
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.MediaList

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SessionScreen(
    viewModel: SessionViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.selectedMediaStem) {
        if (uiState.selectedMediaStem.isEmpty()) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        } else {
            val contentKey = uiState.selectedMediaStem
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, contentKey)
        }
    }

    LaunchedEffect(uiState.errors) {
        uiState.errors.forEach { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.safeContentPadding().fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        MediasNavigationWrapper(
            currentMediaStateEq = uiState.mediaStateEq,
            navigateToTopLevelDestination = { viewModel.navigateToOtherSessionMediaState(navController, it) }
        ) {
            ListDetailPaneScaffold(
                modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues),
                directive = navigator.scaffoldDirective,
                scaffoldState = navigator.scaffoldState,
                listPane = {
                    val isDetailVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

                    AnimatedPane {
                        if (uiState.loading) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(64.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }
                        } else {
                            MediaList(
                                mediaEntries = uiState.filteredMedias.entries,
                                currentSelectedMediaStem = if (isDetailVisible) uiState.selectedMediaStem else "",
                                session = uiState.session,
                                onItemClick = { item -> viewModel.navigateTo(item.key) },
                                navigateBack = { navController.navigateUp() }
                            )
                        }
                    }
                },
                detailPane = {
                    AnimatedPane {
                        navigator.currentDestination?.contentKey?.let {
                            MediaScreen(
                                viewModel = viewModel.buildMediaViewModel(it),
                                navigateBack = { viewModel.navigateTo("") }
                            )
                        } ?: NoMediaScreen()
                    }
                }
            )
        }
    }
}
package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.component.LoadingSuspense
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data.dummyNewSessionEntry
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionCreateForm
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionDetails
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionList
import org.openapitools.client.models.Session

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun RecentSessionsScreen(
    viewModel: SessionsViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navigator = rememberListDetailPaneScaffoldNavigator<Map.Entry<String, Session>>()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.selectedSessionEntryKey) {
        // Navigate to the detail pane with the passed item

        if (uiState.selectedSessionEntryKey.isEmpty()) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        } else {
            uiState.sessions
                .find { it.key == uiState.selectedSessionEntryKey }
                .let {
                    val item = when(uiState.selectedSessionEntryKey) {
                        dummyNewSessionEntry.key -> dummyNewSessionEntry
                        else -> it
                    }

                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                }
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
        ListDetailPaneScaffold(
            modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues),
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            listPane = {
                val isDetailVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

                AnimatedPane {
                    SessionList(
                        sessionEntries = uiState.sessions,
                        currentSelectedSessionKey = if (isDetailVisible) uiState.selectedSessionEntryKey else "",
                        onItemClick = { item -> viewModel.navigateTo(item.key) }
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    LoadingSuspense(uiState.loading) {
                        val isListVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
                        val navigateBack = when {
                            isListVisible -> null
                            else -> { -> viewModel.navigateTo("") }
                        }

                        // Show the detail pane content if selected item is available
                        navigator.currentDestination?.contentKey?.let {
                            when (it.key) {
                                "new_session" -> SessionCreateForm(
                                    onCreate = viewModel::createSession,
                                    navigateBack = navigateBack
                                )

                                else -> SessionDetails(
                                    sessionEntry = it,
                                    onClick = { endpoint, session -> viewModel.openSession(navController, endpoint, session) },
                                    onDelete = viewModel::deleteSession,
                                    navigateBack = navigateBack
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

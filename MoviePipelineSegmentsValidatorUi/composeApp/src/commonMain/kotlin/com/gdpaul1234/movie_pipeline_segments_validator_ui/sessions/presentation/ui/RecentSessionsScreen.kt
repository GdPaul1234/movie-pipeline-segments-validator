package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.data.dummyNewSessionEntry
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionCreateForm
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionDetails
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionList
import org.openapitools.client.models.Session

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun RecentSessionsScreen(viewModel: SessionsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val navigator = rememberListDetailPaneScaffoldNavigator<Map.Entry<String, Session>>()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.selectedSessionEntryKey) {
        // Navigate to the detail pane with the passed item
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

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        listPane = {
            val isDetailVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

            AnimatedPane {
                SessionList(
                    sessionEntries = uiState.sessions,
                    currentSelectedSessionKey = if (isDetailVisible) uiState.selectedSessionEntryKey else "",
                    onItemClick = { item -> viewModel.setSelectedSessionEntryKey(item.key) }
                )
            }
        },
        detailPane = {
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
                    // Show the detail pane content if selected item is available
                    navigator.currentDestination?.contentKey?.let {
                        when (it.key) {
                            "new_session" -> SessionCreateForm(
                                navigator = navigator,
                                scope = scope,
                                onCreate = viewModel::createSession
                            )

                            else -> SessionDetails(
                                navigator = navigator,
                                scope = scope,
                                sessionEntry = it,
                                onDelete = viewModel::deleteSession
                            )
                        }
                    }
                }
            }
        },
    )
}

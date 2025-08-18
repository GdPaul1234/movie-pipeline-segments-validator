package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.network.SessionsService
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionCreateForm
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionDetails
import com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component.SessionList
import kotlinx.coroutines.launch
import org.openapitools.client.models.Session

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun RecentSessionsScreen(
    sessionsRepository: SessionsRepository
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Map.Entry<String, Session>>()
    val scope = rememberCoroutineScope()

    val sessions by sessionsRepository.getRecents().collectAsStateWithLifecycle(setOf())

    var selectedSessionEntryKey: String by rememberSaveable { mutableStateOf("") }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        listPane = {
            val currentSelectedSessionEntryKey = selectedSessionEntryKey
            val isDetailVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

            AnimatedPane {
                SessionList(
                    sessionEntries = sessions,
                    currentSelectedSessionKey = if (isDetailVisible) currentSelectedSessionEntryKey else "",
                    onItemClick = { item ->
                        selectedSessionEntryKey = item.key

                        // Navigate to the detail pane with the passed item
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                // Show the detail pane content if selected item is available
                navigator.currentDestination?.contentKey?.let {
                    when (it.key) {
                        "new_session" -> SessionCreateForm(
                            navigator = navigator,
                            scope = scope,
                            onCreate = { endpoint, rootPath ->
                                scope.launch { SessionsService(endpoint, sessionsRepository).createSession(rootPath) }
                            }
                        )
                        else -> SessionDetails(
                            navigator = navigator,
                            scope = scope,
                            sessionEntry = it,
                            onDelete = { endpoint, session ->
                                scope.launch { SessionsService(endpoint, sessionsRepository).deleteSession(session.id) }
                            }
                        )
                    }
                }
            }
        },
    )
}

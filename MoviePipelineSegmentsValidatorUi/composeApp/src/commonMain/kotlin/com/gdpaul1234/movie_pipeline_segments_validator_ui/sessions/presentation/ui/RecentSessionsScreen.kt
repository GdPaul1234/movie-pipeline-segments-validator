package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.database.SessionsRepository
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
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<Map.Entry<String, Session>>()
    val customScaffoldDirective = customPaneScaffoldDirective(currentWindowAdaptiveInfo())
    val scope = rememberCoroutineScope()

    val sessions by sessionsRepository.getRecents().collectAsStateWithLifecycle(setOf())

    ListDetailPaneScaffold(
        directive = customScaffoldDirective,
        scaffoldState = scaffoldNavigator.scaffoldState,
        listPane = {
            AnimatedPane {
                SessionList(
                    sessionEntries = sessions,
                    onItemClick = { item ->
                        // Navigate to the detail pane with the passed item
                        scope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                // Show the detail pane content if selected item is available
                scaffoldNavigator.currentDestination?.contentKey?.let {
                    when (it.key) {
                        "new_session" -> SessionCreateForm(onCreate = { endpoint, rootPath -> println("$rootPath@$endpoint") /* TODO */ })
                        else -> SessionDetails(sessionEntry = it)
                    }
                }
            }
        },
    )
}

fun customPaneScaffoldDirective(currentWindowAdaptiveInfo: WindowAdaptiveInfo): PaneScaffoldDirective {
    val horizontalPartitions = when {
        currentWindowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        currentWindowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> 2
        else -> 1
    }

    return PaneScaffoldDirective(
        maxHorizontalPartitions = horizontalPartitions,
        horizontalPartitionSpacerSize = 16.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 8.dp,
        defaultPanePreferredWidth = 320.dp,
        excludedBounds = emptyList()
    )
}
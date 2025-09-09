@file:OptIn(ExperimentalTime::class)

package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.component.LoadingSuspense
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component.MediaPreviewParameter
import kotlinx.coroutines.launch
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.openapitools.client.models.Media
import org.openapitools.client.models.Session
import java.io.File
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SessionDetails(
    sessionKey: String,
    loadSession: suspend (String, String) -> Session,
    onClick: (endpoint: String, session: Session) -> Unit,
    onDelete: (endpoint: String, session: Session) -> Unit,
    navigateBack: (() -> Unit)?
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var session by remember { mutableStateOf<Session?>(null) }
    val endpoint = remember(sessionKey) { sessionKey.split("@").last() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionKey) {
        scope.launch {
            val (sessionId, endpoint) = sessionKey.split("@")
            session = loadSession(endpoint, sessionId)
        }
    }

    LoadingSuspense(session == null) {
        session?.let { session ->
            val sessionEntry = remember(sessionKey, session) { mapOf(sessionKey to session).entries.first() }

            Scaffold(
                topBar = {
                    LargeTopAppBar(
                        scrollBehavior = topAppBarScrollBehavior,
                        title = {
                            Text(
                                text = session.rootPath,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            if (navigateBack != null) {
                                IconButton(onClick = { navigateBack() }) {
                                    Icon(
                                        painterResource(Res.drawable.arrow_back_24px),
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(Res.string.load_session)) },
                        icon = { Icon(painterResource(Res.drawable.open_in_browser_24px), null) },
                        onClick = { onClick(endpoint, session) },
                    )
                }
            ) { paddingValues ->
                LazyVerticalGrid (
                    columns = GridCells.Adaptive((WIDTH_DP_MEDIUM_LOWER_BOUND / 2).dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp)
                        .consumeWindowInsets(paddingValues)
                ) {
                    item { InfoSection(sessionEntry, onDelete) }
                    item { StatsSection(sessionEntry) }
                    item { Spacer(Modifier.padding(bottom = 64.dp)) }
                }
            }
        }
    }
}

@Composable
@Preview
private fun InfoSection(
    @PreviewParameter(SessionEntryPreviewParameterProvider::class) sessionEntry: Map.Entry<String, Session>,
    onDelete: ((endpoint: String, session: Session) -> Unit)?
) {
    val (key, session) = sessionEntry
    val (id, endpoint) = key.split("@")
    val infos = listOf(
        stringResource(Res.string.endpoint) to endpoint,
        stringResource(Res.string.session_id) to id,
        stringResource(Res.string.created_at) to session.createdAt.toString(),
        stringResource(Res.string.updated_at) to session.updatedAt.toString(),
        stringResource(Res.string.medias_number) to session.medias.size.toString()
    )

    val listItemColors = ListItemDefaults.colors(CardDefaults.cardColors().containerColor)

    Card(modifier = Modifier.padding(bottom = 16.dp)) {
        ListItem(
            colors = listItemColors,
            headlineContent = {
                Text(
                    text = stringResource(Res.string.infos),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        )

        infos.forEach { (label, value) ->
            ListItem(
                colors = listItemColors,
                headlineContent = { Text(label) },
                supportingContent = { Text(value) }
            )
        }

        TextButton(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp),
            onClick = { onDelete?.let { it(endpoint, session) } }
        ) {
            Text(
                text = stringResource(Res.string.delete_session),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
@Preview
private fun StatsSection (
    @PreviewParameter(SessionEntryPreviewParameterProvider::class) sessionEntry: Map.Entry<String, Session>,
    isPreview: Boolean = LocalInspectionMode.current
) {
    val listItemColors = ListItemDefaults.colors(CardDefaults.cardColors().containerColor)

    Card {
        ListItem(
            colors = listItemColors,
            headlineContent = {
                Text(
                    text = stringResource(Res.string.stats),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        )

        Media.State.entries.forEach { mediaState ->
            val stateLabel = when {
                isPreview -> mediaState.value
                else -> stringResource(Res.allStringResources["stats_${mediaState.value}"]!!)
            }

            val count = sessionEntry.value.medias.count { it.value.state == mediaState }

            ListItem(
                colors = listItemColors,
                headlineContent = { Text(stateLabel) },
                supportingContent = { Text(count.toString()) }
            )
        }
    }
}

class SessionEntryPreviewParameterProvider : PreviewParameterProvider<Map.Entry<String, Session>> {
    override val values = sequenceOf(
        setOf(
            Session(
                id = "02a34f8ac5b24cb09c5bb23ccca034c4",
                createdAt = Clock.System.now() - 5.days,
                updatedAt = Clock.System.now() - 2.days,
                rootPath = "V:\\PVR",
                medias = MediaPreviewParameter().values.associateBy { File(it.filepath).nameWithoutExtension }
            )
        ).associateBy { "${it.id}@http://localhost:8000" }.entries.first()
    )
}
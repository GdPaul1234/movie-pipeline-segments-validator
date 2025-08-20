@file:OptIn(ExperimentalTime::class)

package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.openapitools.client.models.Media
import org.openapitools.client.models.SegmentOutput
import org.openapitools.client.models.Session
import java.io.File
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
@Preview
fun SessionDetails(
    @PreviewParameter(SessionEntryPreviewParameterProvider::class) sessionEntry: Map.Entry<String, Session>,
    onDelete: ((endpoint: String, session: Session) -> Unit)?,
    navigateBack: (() -> Unit)?
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                scrollBehavior = topAppBarScrollBehavior,
                title = {
                    Text(
                        text = sessionEntry.value.rootPath,
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
            FloatingActionButton(
                onClick = { /* TODO */ },
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(Res.drawable.open_in_browser_24px),
                        null,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(Res.string.load_session))
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid (
            columns = GridCells.Adaptive((WIDTH_DP_MEDIUM_LOWER_BOUND / 2).dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .consumeWindowInsets(paddingValues)
        ) {
            item { InfoSection(sessionEntry, onDelete) } // TODO Confirm before delete
            item { StatsSection(sessionEntry) }
            item { Spacer(Modifier.padding(bottom = 64.dp)) }
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
    val infos =listOf(
        stringResource(Res.string.endpoint) to endpoint,
        stringResource(Res.string.session_id) to id,
        stringResource(Res.string.created_at) to session.createdAt.toString(),
        stringResource(Res.string.updated_at) to session.updatedAt.toString(),
        stringResource(Res.string.medias_number) to session.medias.size.toString()
    )

    val listItemColors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceContainerHighest)

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
    val listItemColors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceContainerHighest)

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
            val stateLabel = when (isPreview) {
                true -> mediaState.value
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
                medias = setOf(
                    Media(
                        filepath = "V:\\PVR\\Channel 1_Movie Name_2022-12-05-2203-20.ts",
                        state = Media.State.waiting_segment_review,
                        title = "Movie Name, le titre long.mp4",
                        skipBackup = false,
                        importedSegments = mapOf(
                            "auto" to "00:00:00.000-01:05:54.840,00:42:38.980-01:49:59.300,01:05:54.840-01:49:59.300",
                            "result_2024-10-05T11:40:39.732479" to "00:25:26.000-00:34:06.000,00:40:10.000-01:01:23.000,01:07:34.000-01:17:59.000"
                        ),
                        segments = listOf(
                            SegmentOutput(start = 1526.0, end = 3246.0, duration = 1720.0)
                        )
                    )
                ).associateBy { File(it.filepath).nameWithoutExtension }
            )
        ).associateBy { "${it.id}@http://localhost:8000" }.entries.first()
    )
}
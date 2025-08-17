@file:OptIn(ExperimentalTime::class)

package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.openapitools.client.models.Session
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SessionList(
    @PreviewParameter(SessionEntriesPreviewParameterProvider::class) sessionEntries: Set<Map.Entry<String, Session>>,
    @PreviewParameter(CurrentSelectedSessionKeyPreviewParameterProvider::class) currentSelectedSessionKey: String,
    onItemClick: ((Map.Entry<String, Session>) -> Unit)?
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val dummyNewSessionEntry = mapOf(
        "new_session" to Session(
            id = "new_session",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now() ,
            rootPath = "null",
            medias = emptyMap()
        )
    ).entries.first()

    @Composable
    fun listContentBackgroundColor(sessionEntry: Map.Entry<String, Session>) =
        when(sessionEntry.key == currentSelectedSessionKey) {
            true -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        }


    Scaffold(
        topBar = {
            LargeTopAppBar(
                scrollBehavior = topAppBarScrollBehavior,
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            item {
                ListItem(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { onItemClick?.let { it(dummyNewSessionEntry) } },
                    colors = ListItemDefaults.colors(listContentBackgroundColor(dummyNewSessionEntry)),
                    headlineContent = { Text(stringResource(Res.string.create_session)) },
                    supportingContent = { Text(stringResource(Res.string.create_session_supporting_text)) },
                )
            }

            if (sessionEntries.isNotEmpty()) {
                item {
                    ListItem(
                        modifier = Modifier.offset(y = 16.dp),
                        headlineContent = {
                            Text(
                                text = stringResource(Res.string.recent_sessions),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }

                items(
                    items = sessionEntries.sortedByDescending { it.value.updatedAt },
                    key = { it.key }
                ) { sessionEntry ->
                    val (key, session) = sessionEntry
                    val (id, endpoint) = key.split("@")
                    val title = "${id.substring(0..16)}@${endpoint.substringAfter("://")}"

                    ListItem(
                        modifier = Modifier.clickable { onItemClick?.let { it(sessionEntry) } },
                        colors = ListItemDefaults.colors(listContentBackgroundColor(sessionEntry)),
                        headlineContent = {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(session.rootPath)
                                Text("${(Clock.System.now() - session.updatedAt).inWholeSeconds.seconds} ago")
                            }
                        },
                        supportingContent = {
                            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    )
                }
            }
        }
    }
}

private class SessionEntriesPreviewParameterProvider : PreviewParameterProvider<Set<Map.Entry<String, Session>>> {
    override val values = sequenceOf(
        setOf(
            Session(
                id = "02a34f8ac5b24cb09c5bb23ccca034c4",
                createdAt = Clock.System.now() - 5.days,
                updatedAt = Clock.System.now() - 2.days,
                rootPath = "V:\\PVR",
                medias = emptyMap()
            ),
            Session(
                id = "35e69f9ee84f494d9dcc6c62a964b171",
                createdAt = Clock.System.now() - 3.days,
                updatedAt = Clock.System.now() - 6.hours,
                rootPath = "V:\\PVR",
                medias = emptyMap()
            ),
            Session(
                id = "75c95991c4cc4fb7a5331a481e7ea66b",
                createdAt = Clock.System.now() - 1.days,
                updatedAt = Clock.System.now() - 5.minutes,
                rootPath = "V:\\PVR",
                medias = emptyMap()
            )
        ).associateBy { "${it.id}@http://localhost:8000" }.entries
    )
}


private class CurrentSelectedSessionKeyPreviewParameterProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(
        "new_session",
        "35e69f9ee84f494d9dcc6c62a964b171",
        ""
    )
}
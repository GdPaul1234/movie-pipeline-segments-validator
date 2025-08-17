@file:OptIn(ExperimentalTime::class)

package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
@Preview
fun SessionList(
    @PreviewParameter(SessionEntriesPreviewParameterProvider::class) sessionEntries: Set<Map.Entry<String, Session>>,
    onItemClick: ((Map.Entry<String, Session>) -> Unit)?
) {
    val dummyNewSessionEntry = mapOf(
        "new_session" to Session(
            id = "new_session",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now() ,
            rootPath = "null",
            medias = emptyMap()
        )
    ).entries.first()

    Card {
        LazyColumn {
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
            }

            item {
                ListItem(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onItemClick?.let { it(dummyNewSessionEntry) } },
                    headlineContent = { Text(stringResource(Res.string.create_session)) },
                    supportingContent = { Text(stringResource(Res.string.create_session_supporting_text)) }
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
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onItemClick?.let { it(sessionEntry) } },
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

class SessionEntriesPreviewParameterProvider : PreviewParameterProvider<Set<Map.Entry<String, Session>>> {
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

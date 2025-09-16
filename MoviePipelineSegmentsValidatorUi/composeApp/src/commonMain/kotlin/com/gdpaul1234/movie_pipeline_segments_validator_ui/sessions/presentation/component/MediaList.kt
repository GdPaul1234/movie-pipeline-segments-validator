package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.util.getMediaStem
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.Res
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.arrow_back_24px
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.openapitools.client.models.Media
import org.openapitools.client.models.Session

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MediaList(
    @PreviewParameter(SessionPreviewParameterProvider::class) session: Session,
    @PreviewParameter(MediasListPreviewParameterProvider::class) medias: Collection<Media>,
    @PreviewParameter(CurrentSelectedMediaStemPreviewParameterProvider::class) currentSelectedMediaStem: String?,
    onItemClick: ((String) -> Unit)?,
    navigateBack: (() -> Unit)?
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val containerColor = MaterialTheme.colorScheme.surfaceContainer

    @Composable
    fun listItemColors(mediaStem: String) =
        ListItemDefaults.colors(
            containerColor = when(mediaStem == currentSelectedMediaStem) {
                true -> MaterialTheme.colorScheme.primary
                else -> containerColor
            },
            headlineColor = when(mediaStem == currentSelectedMediaStem) {
                true -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.onSurface
            },
            supportingColor = when(mediaStem == currentSelectedMediaStem) {
                true -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

    val sortedMedias = remember(medias) { medias.sortedBy { it.filepath } }

    Scaffold(
        containerColor = containerColor,
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.largeTopAppBarColors().copy(
                    containerColor = containerColor,
                    scrolledContainerColor = containerColor
                ),
                scrollBehavior = topAppBarScrollBehavior,
                title = { Text(text = session.rootPath, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            items(items = sortedMedias, key = { it.filepath }) { media ->
                val mediaStem = getMediaStem(media, session)

                ListItem(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .clickable { onItemClick?.let { it(mediaStem) } },
                    colors = listItemColors(mediaStem),
                    headlineContent = { Text(media.title) },
                    supportingContent = {
                        Text(
                            media.filepath,
                            maxLines = 1,
                            overflow = TextOverflow.StartEllipsis
                        )
                    }
                )
            }
        }
    }
}

private class SessionPreviewParameterProvider : PreviewParameterProvider<Session> {
    override val values = SessionEntryPreviewParameterProvider().values.map { it.value }
}

private class MediasListPreviewParameterProvider : PreviewParameterProvider<List<Media>> {
    override val values = SessionPreviewParameterProvider().values.map {
        it.medias.values.toList()
    }
}

private class CurrentSelectedMediaStemPreviewParameterProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(
        "02a34f8ac5b24cb09c5bb23ccca034c4",
        ""
    )
}
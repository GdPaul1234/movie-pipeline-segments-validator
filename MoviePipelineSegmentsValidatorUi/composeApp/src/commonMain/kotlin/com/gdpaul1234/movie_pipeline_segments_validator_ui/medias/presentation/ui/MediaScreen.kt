package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component.MediaActionsBottomBar
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component.MediaActionsTopAppBar
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component.MediaRecordingMetadataCard
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component.ValidateSegmentsButton
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openapitools.client.models.Media
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MediaScreen(
    viewModel: MediaViewModel,
    navigateBack: (() -> Unit)?,
    navigateToDetails: (() -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val adaptiveInfo = currentWindowAdaptiveInfo()

    val isReadOnly = uiState.media?.let {
        listOf(Media.State.media_processing, Media.State.media_processed).contains(it.state)
    } ?: true

    LaunchedEffect(uiState.errors) {
        uiState.errors.forEach { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(contentAlignment = Alignment.TopCenter) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    scrollBehavior = topAppBarScrollBehavior,
                    title = {
                        if (isReadOnly) {
                            Text(viewModel.getTitle())
                        } else {
                            TextField(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                value = viewModel.getTitle(),
                                onValueChange = viewModel::setTitle,
                                singleLine = true
                            )
                        }
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
                    },
                    actions = {
                        if (!adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
                            MediaActionsTopAppBar(
                                segmentsView = uiState.segmentsView,
                                segmentsSelectionMode = uiState.segmentsSelectionMode,
                                toggleSegmentsView = viewModel::toggleSegmentsView,
                                setSelectionMode = viewModel::setSelectionMode,
                                importSegments = {/* TODO import segments */}
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
                    ValidateSegmentsButton(onClick = { /* TODO validate segments */ })
                }
            },
            bottomBar = {
                if (adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
                    MediaActionsBottomBar(
                        segmentsView = uiState.segmentsView,
                        segmentsSelectionMode = uiState.segmentsSelectionMode,
                        toggleSegmentsView = viewModel::toggleSegmentsView,
                        setSelectionMode = viewModel::setSelectionMode,
                        importSegments = {/* TODO import segments */}
                    )
                }
            },
            modifier = Modifier
                .safeContentPadding()
                .widthIn(max = WIDTH_DP_LARGE_LOWER_BOUND.dp)
                .fillMaxSize()
            ,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            val rootModifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .padding(24.dp, 8.dp)
                .consumeWindowInsets(paddingValues)

            if (uiState.loading) {
                Box(contentAlignment = Alignment.Center, modifier = rootModifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            } else {
                LazyColumn(modifier = rootModifier) {
                    item {
                        ListItem(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .then(
                                    if (isReadOnly) Modifier
                                    else Modifier.clickable { viewModel.setSkipBackup(!viewModel.getSkipBackup()) }
                                ),
                            headlineContent = { Text(stringResource(Res.string.skip_backup_label)) },
                            supportingContent = { Text(stringResource(Res.string.skip_backup_supporting_text)) },
                            trailingContent = {
                                Switch(
                                    checked = viewModel.getSkipBackup(),
                                    onCheckedChange = if (isReadOnly) null else viewModel::setSkipBackup
                                )
                            },
                        )
                    }

                    item {
                        uiState.recordingMetadata?.let { recordingMetadata ->
                            MediaRecordingMetadataCard(
                                recordingMetadata = recordingMetadata,
                                duration = uiState.duration,
                                navigateToDetails = navigateToDetails
                            )
                        } ?: Card(modifier = Modifier.padding(bottom = 16.dp)) {
                            if (navigateToDetails != null) {
                                ListItem(
                                    modifier = Modifier.clickable { navigateToDetails() },
                                    colors = ListItemDefaults.colors(CardDefaults.cardColors().containerColor),
                                    headlineContent = {
                                        Text(
                                            text = stringResource(Res.string.media_more_details_label),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    item {
                        uiState.duration?.let { duration ->
                            Box(
                                Modifier
                                    .clip(ShapeDefaults.Large)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .aspectRatio(21f / 9f)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = listOf(
                                        uiState.position.seconds.inWholeSeconds.seconds,
                                        duration.seconds.inWholeSeconds.seconds
                                    ).joinToString(" / "),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .offset(16.dp, (-16).dp)
                                        .clip(ShapeDefaults.Small)
                                        .background(MaterialTheme.colorScheme.surfaceDim)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Slider(
                                modifier = Modifier.fillMaxWidth(),
                                valueRange = 0f..duration.toFloat(),
                                value = uiState.position.toFloat(),
                                onValueChange = viewModel::setPosition
                            )
                        }
                    }
                }
            }
        }
    }
}


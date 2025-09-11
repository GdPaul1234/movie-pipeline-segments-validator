package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.component.AsyncImageWithPrevious
import com.gdpaul1234.movie_pipeline_segments_validator_ui.core.presentation.component.LoadingSuspense
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.data.SegmentsView
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.time.debounce
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openapitools.client.models.Media
import org.openapitools.client.models.MediaMetadata
import org.openapitools.client.models.SegmentOutput
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaDuration

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MediaScreen(
    viewModel: MediaViewModel,
    navigateToMediaStem: (String) -> Unit,
    navigateBack: (() -> Unit)?,
    navigateToDetails: (() -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    val isReadOnly = uiState.media?.let {
        it.state in listOf(Media.State.waiting_metadata, Media.State.segment_reviewed, Media.State.media_processing, Media.State.media_processed)
    } ?: true

    var showImportSegmentsDialog by remember { mutableStateOf(false) }

    val title by remember { derivedStateOf { uiState.media?.title ?: "" } }
    val skipBackup by remember { derivedStateOf { uiState.media?.skipBackup ?: false } }

    LaunchedEffect(uiState.errors) {
        uiState.errors.forEach { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BoxWithConstraints {
        val isSmallScreen = minWidth < WIDTH_DP_MEDIUM_LOWER_BOUND.dp
        val canShowEditSegmentsSideToolbar = minWidth >= (WIDTH_DP_LARGE_LOWER_BOUND + 130).dp &&
                minHeight >= (HEIGHT_DP_MEDIUM_LOWER_BOUND + 48).dp

        val segmentsEditOnClick = SegmentsEditOnClick(
            onAddSegmentClick = viewModel::addSegmentAtCurrentPosition,
            onRemoveSegmentsClick = viewModel::removeSelectedSegments,
            onMergeSegmentsClick = viewModel::mergeSelectedSegments,
            onGoToStartOfSegmentClick = viewModel::setPositionAtStartOfSelectedSegment,
            onGoToEndOfSegmentClick = viewModel::setPositionAtEndOfSelectedSegment,
            onSetSegmentStart = viewModel::setSelectedSegmentStart,
            onSetSegmentEnd = viewModel::setSelectedSegmentEnd
        )

        Scaffold(
            topBar = {
                LargeTopAppBar(
                    scrollBehavior = topAppBarScrollBehavior,
                    title = { TitleSection(isReadOnly, title, viewModel::setTitle) },
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
                        if (isSmallScreen) {
                            MediaActionsTopAppBar(
                                segmentsView = uiState.segmentsView,
                                segmentsSelectionMode = uiState.segmentsSelectionMode,
                                toggleSegmentsView = viewModel::toggleSegmentsView,
                                setSelectionMode = viewModel::setSelectionMode,
                                importSegments = { showImportSegmentsDialog = true },
                                isReadOnly = isReadOnly
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!isReadOnly && isSmallScreen) {
                    ValidateSegmentsButton(onClick = { viewModel.validateSegments(navigateToMediaStem) })
                }
            },
            bottomBar = {
                if (!isSmallScreen) {
                    MediaActionsBottomBar(
                        segmentsView = uiState.segmentsView,
                        segmentsSelectionMode = uiState.segmentsSelectionMode,
                        toggleSegmentsView = viewModel::toggleSegmentsView,
                        setSelectionMode = viewModel::setSelectionMode,
                        importSegments = { showImportSegmentsDialog = true },
                        validateSegments = { viewModel.validateSegments(navigateToMediaStem) },
                        isReadOnly = isReadOnly
                    )
                }
            },
            modifier = Modifier
                .safeContentPadding()
                .widthIn(max = WIDTH_DP_LARGE_LOWER_BOUND.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize()
            ,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            val rootModifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .padding(24.dp, 8.dp)
                .consumeWindowInsets(paddingValues)

            LoadingSuspense(uiState.loading) {
                LazyColumn(modifier = rootModifier) {
                    item { SetSkipBackupSection(isReadOnly, skipBackup, viewModel::setSkipBackup) }
                    item { MediaMetadataSection(uiState.recordingMetadata, uiState.duration, navigateToDetails) }

                    item {
                        uiState.duration?.let { duration ->
                            MediaPreviewSection(
                                getFrameUrl = viewModel::getFrameUrl,
                                position = uiState.position,
                                duration = duration,
                                setPosition = viewModel::setPosition,
                                isSmallScreen = isSmallScreen
                            )

                            uiState.media?.segments?.let { segments ->
                                SegmentsEditSection(
                                    segmentsView = uiState.segmentsView,
                                    segments = segments,
                                    selectedSegments = uiState.selectedSegments,
                                    position = uiState.position,
                                    duration = duration,
                                    toggleSegment = viewModel::toggleSegment,
                                    segmentsEditOnClick = segmentsEditOnClick,
                                    canShowEditSegmentsSideToolbar = canShowEditSegmentsSideToolbar,
                                    isReadOnly = isReadOnly,
                                    isSmallScreen = isSmallScreen
                                )
                            }
                        }
                    }
                }
            }
        }

        if (canShowEditSegmentsSideToolbar) {
            SegmentsEditVerticalToolbar(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
                selectedSegments = uiState.selectedSegments,
                segmentsEditOnClick = segmentsEditOnClick,
                isReadOnly = isReadOnly
            )
        }
    }

    if (showImportSegmentsDialog) {
        uiState.media?.let { media ->
            SegmentsImportDialog(
                importedSegments = media.importedSegments,
                onCancel = { showImportSegmentsDialog = false },
                onConfirm = { selectedDetectorKey ->
                    showImportSegmentsDialog = false
                    viewModel.importSegments(selectedDetectorKey)
                }
            )
        }
    }
}

@Composable
private fun TitleSection(
    isReadOnly: Boolean,
    title: String,
    setTitle: (String) -> Unit
) {
    if (isReadOnly) {
        Text(title)
    } else {
        TextField(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            value = title,
            onValueChange = setTitle,
            singleLine = true
        )
    }
}

@Composable
private fun SetSkipBackupSection(
    isReadOnly: Boolean,
    skipBackup: Boolean,
    setSkipBackup: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .then(
                if (isReadOnly) Modifier
                else Modifier.clickable { setSkipBackup(!skipBackup) }
            ),
        headlineContent = { Text(stringResource(Res.string.skip_backup_label)) },
        supportingContent = { Text(stringResource(Res.string.skip_backup_supporting_text)) },
        trailingContent = {
            Switch(
                checked = skipBackup,
                onCheckedChange = if (isReadOnly) null else setSkipBackup
            )
        },
    )
}

@Composable
private fun MediaMetadataSection(
    recordingMetadata: MediaMetadata?,
    duration: Double?,
    navigateToDetails: (() -> Unit)?
) {
    recordingMetadata?.let {
        MediaRecordingMetadataCard(
            recordingMetadata = it,
            duration = duration,
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

@OptIn(FlowPreview::class)
@Composable
private fun MediaPreviewSection(
    getFrameUrl: () -> String,
    position: Double,
    duration: Double,
    setPosition: (Number) -> Unit,
    isSmallScreen: Boolean
) {
    val frameUrlFlow = remember { MutableStateFlow(getFrameUrl()) }
    LaunchedEffect(position) { frameUrlFlow.emit(getFrameUrl()) }

    var frameUrl by remember { mutableStateOf(getFrameUrl()) }

    LaunchedEffect(Unit) {
        frameUrlFlow
            .debounce(500.milliseconds.toJavaDuration()) // Wait for 300ms of inactivity
            .distinctUntilChanged() // Emit only if the value has changed
            .collect{ frameUrl = it }
    }

    Box(
        Modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .aspectRatio(16f / 9f)
            .fillMaxHeight()
    ) {
        AsyncImageWithPrevious(url = frameUrl)

        if (!isSmallScreen) {
            MediaPositionToolbar(Modifier.align(Alignment.BottomStart), position, duration, setPosition)
        }
    }

    if (isSmallScreen) {
        MediaPositionToolbar(Modifier, position, duration, setPosition, isSmallScreen = true)
    }

    Slider(
        modifier = Modifier.fillMaxWidth(),
        valueRange = 0f..duration.toFloat(),
        value = position.toFloat(),
        onValueChange = setPosition
    )
}

@Composable
private fun SegmentsEditSection(
    segmentsView: SegmentsView,
    segments: List<SegmentOutput>,
    selectedSegments: Set<SegmentOutput>,
    position: Double,
    duration: Double,
    toggleSegment: (SegmentOutput) -> Unit,
    segmentsEditOnClick: SegmentsEditOnClick,
    canShowEditSegmentsSideToolbar: Boolean,
    isReadOnly: Boolean,
    isSmallScreen: Boolean
) {
    Card(
        modifier = if (canShowEditSegmentsSideToolbar) Modifier else Modifier.padding(top = 16.dp, bottom = 72.dp).fillMaxSize(),
        colors = if (canShowEditSegmentsSideToolbar) CardDefaults.cardColors(Color.Transparent) else CardDefaults.cardColors()
    ) {
        if (!canShowEditSegmentsSideToolbar) {
            SegmentsEditHorizontalToolbar(selectedSegments, segmentsEditOnClick, isSmallScreen, isReadOnly)
        }

        when (segmentsView) {
            SegmentsView.TIMELINE -> SegmentsAsTimeline(
                segments = segments,
                selectedSegments = selectedSegments,
                toggleSegment = toggleSegment,
                position = position,
                duration = duration
            )

            SegmentsView.LIST -> SegmentsAsList(
                modifier = if (canShowEditSegmentsSideToolbar) Modifier else Modifier.padding(8.dp),
                segments = segments,
                selectedSegments = selectedSegments,
                toggleSegment = toggleSegment
            )
        }
    }
}

package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MediaScreen(
    viewModel: MediaViewModel,
    navigateBack: (() -> Unit)?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errors) {
        uiState.errors.forEach { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                scrollBehavior = topAppBarScrollBehavior,
                title = {
                    TextField(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        value = viewModel.getTitle(),
                        onValueChange = viewModel::setTitle,
                        singleLine = true
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
                onClick = { /* TODO validate segments */ },
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(Res.drawable.playlist_add_check_24px),
                        null,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(Res.string.media_validate_segments))
                }
            }
        },
        modifier = Modifier.safeContentPadding().fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        val rootModifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .padding(paddingValues)
            .padding(24.dp, 8.dp)
            .consumeWindowInsets(paddingValues)

        if(uiState.loading) {
            Box(contentAlignment = Alignment.Center, modifier = rootModifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        } else {
            Column(modifier = rootModifier) {
                ListItem(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .clickable { viewModel.setSkipBackup(!viewModel.getSkipBackup()) },
                    headlineContent = { Text(stringResource(Res.string.skip_backup_label)) },
                    supportingContent = { Text(stringResource(Res.string.skip_backup_supporting_text)) },
                    trailingContent = { Switch(checked = viewModel.getSkipBackup(), onCheckedChange = viewModel::setSkipBackup) },
                )

                uiState.recordingMetadata?.apply {
                    Card(modifier = Modifier.padding(bottom = 16.dp)) {
                        val listItemColors = ListItemDefaults.colors(containerColor = CardDefaults.cardColors().containerColor)
                        val metadata = listOf(
                            stringResource(Res.string.media_channel) to channel,
                            stringResource(Res.string.media_start_real) to Instant.fromEpochSeconds(startReal).toString(),
                            stringResource(Res.string.media_stop_real) to Instant.fromEpochSeconds(stopReal).toString(),
                            stringResource(Res.string.media_duration) to (uiState.duration?.seconds?.inWholeSeconds?.seconds?.toString() ?: "N/A")
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(items = metadata, key = { it.first }) { (text, value) ->
                                ListItem(
                                    colors = listItemColors,
                                    headlineContent = { Text(value) },
                                    supportingContent = { Text(text) }
                                )
                            }

                            item {
                                ListItem(
                                    colors = listItemColors,
                                    headlineContent = {
                                        TooltipBox(
                                            positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                                            tooltip = { PlainTooltip { Text(errorMessage) } },
                                            state = rememberTooltipState()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("$nbDataErrors")
                                                Icon(
                                                    painterResource(Res.drawable.info_24px),
                                                    contentDescription = "Info",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    supportingContent = { Text(stringResource(Res.string.media_nb_data_errors)) }
                                )
                            }
                        }
                    }

                }
            }

        }
    }
}
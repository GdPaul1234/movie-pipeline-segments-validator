package com.gdpaul1234.movie_pipeline_segments_validator_ui.sessions.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.InvalidPathException
import java.nio.file.Paths

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
@Preview
fun SessionCreateForm (
    onCreate: ((endpoint: String, rootPath: String) -> Unit)?,
    navigateBack: (() -> Unit)?
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                scrollBehavior = topAppBarScrollBehavior,
                title = { Text(stringResource(Res.string.create_session_label)) },
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
        var endpoint by rememberSaveable { mutableStateOf("") }
        var rootPath by rememberSaveable { mutableStateOf("") }

        val endpointHasErrors by remember { derivedStateOf { endpoint.isNotBlank() && !urlIsValid(endpoint) } }
        val rootPathHasErrors by remember { derivedStateOf { rootPath.isNotBlank() && !pathIsValid(rootPath) } }

        // Button is enabled only when both fields have a non‑blank value
        val canCreate = endpoint.isNotBlank() && rootPath.isNotBlank() && !endpointHasErrors && !rootPathHasErrors

        Box(
            modifier = Modifier
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .widthIn(max = WIDTH_DP_MEDIUM_LOWER_BOUND.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                TextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text(stringResource(Res.string.endpoint)) },
                    singleLine = true,
                    isError = endpointHasErrors,
                    supportingText = { if (endpointHasErrors) Text(stringResource(Res.string.endpoint_format_error)) },
                    modifier = Modifier.fillMaxWidth().requiredHeight(72.dp)
                )

                TextField(
                    value = rootPath,
                    onValueChange = { rootPath = it },
                    label = { Text(stringResource((Res.string.root_path))) },
                    singleLine = true,
                    isError = rootPathHasErrors,
                    supportingText = { if (rootPathHasErrors) Text(stringResource(Res.string.root_path_format_error)) },
                    modifier = Modifier.fillMaxWidth().requiredHeight(72.dp)
                )

                Button(
                    onClick = { onCreate?.let { it(endpoint, rootPath) } },
                    enabled = canCreate
                ) { Text(stringResource(Res.string.create_session_button)) }
            }
        }
    }
}

private fun urlIsValid(url: String) =
    try {
        URI(url).toURL()
        true
    } catch (e: Exception) {
        when (e) {
            is IllegalArgumentException,
            is MalformedURLException,
            is URISyntaxException -> false
            else -> throw e
        }
    }


private fun pathIsValid(path: String) =
    try {
        Paths.get(path)
        true
    } catch (_: InvalidPathException) {
        false
    }
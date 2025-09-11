package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.Res
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.confirm_label
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.dismiss_label
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.segments_import
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentsImportDialog(
    importedSegments: Map<String, String>,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedDetectorKey by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedDetectorKey) },
                enabled = selectedDetectorKey.isNotEmpty()
            ) {
                Text(stringResource(Res.string.confirm_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(Res.string.dismiss_label)) }
        },
        title = { Text(stringResource(Res.string.segments_import)) },
        text = {
            LazyColumn(Modifier.heightIn(max = HEIGHT_DP_MEDIUM_LOWER_BOUND.dp)) {
                items(importedSegments.entries.toList(), { it.key }) { entry ->
                    ListItem(
                        modifier = Modifier.selectable(
                            selected = entry.key == selectedDetectorKey,
                            onClick = { selectedDetectorKey = entry.key }
                        ),
                        colors = ListItemDefaults.colors(AlertDialogDefaults.containerColor),
                        headlineContent = { Text(entry.key) },
                        supportingContent = { Text(entry.value) },
                        trailingContent = {
                            RadioButton(
                                selected = entry.key == selectedDetectorKey,
                                onClick = { selectedDetectorKey = entry.key }
                            )
                        }
                    )

                }
            }
        }
    )
}

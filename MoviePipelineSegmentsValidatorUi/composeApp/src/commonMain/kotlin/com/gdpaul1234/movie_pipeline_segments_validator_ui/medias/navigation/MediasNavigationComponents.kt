package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.navigation

/**
 * Inspired of Reply android jetpack compose sample
 * https://github.com/android/compose-samples/blob/main/Reply/app/src/main/java/com/example/reply/ui/navigation/ReplyNavigationComponents.kt
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.util.MediasNavigationType
import kotlinx.coroutines.launch
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openapitools.client.models.Media

private fun WindowSizeClass.isMedium() =
    isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

fun getNavLayoutType(adaptiveInfo: WindowAdaptiveInfo) =
    when {
        adaptiveInfo.windowPosture.isTabletop -> MediasNavigationType.NAVIGATION_BAR
        adaptiveInfo.windowSizeClass.isMedium() -> MediasNavigationType.NAVIGATION_RAIL
        else -> MediasNavigationType.NAVIGATION_BAR
    }

@Composable
fun MediasNavigationWrapper(
    currentMediaStateEq: Media.State,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
    content: @Composable (MediasNavigationType) -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val navLayoutType = getNavLayoutType(adaptiveInfo)

    Scaffold(
        bottomBar = {
            if (navLayoutType == MediasNavigationType.NAVIGATION_BAR) {
                MediaBottomNavigationBar(
                    currentMediaStateEq = currentMediaStateEq,
                    navigateToTopLevelDestination = navigateToTopLevelDestination,
                )
            }
        },
        contentWindowInsets = WindowInsets.displayCutout
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            Row {
                if (navLayoutType == MediasNavigationType.NAVIGATION_RAIL) {
                    MediaNavigationRail(
                        currentMediaStateEq = currentMediaStateEq,
                        navigateToTopLevelDestination = navigateToTopLevelDestination,
                    )
                }

                // Content area to show the NavigationRail in context
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    content(navLayoutType)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaNavigationRail(
    currentMediaStateEq: Media.State,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
) {
    val state = rememberWideNavigationRailState()
    val scope = rememberCoroutineScope()
    val headerDescription = when {
        state.targetValue == WideNavigationRailValue.Expanded -> stringResource(Res.string.close_drawer)
        else -> stringResource(Res.string.navigation_drawer)
    }

    ModalWideNavigationRail(
        state = state,
        colors = WideNavigationRailDefaults.colors(MaterialTheme.colorScheme.inverseOnSurface),
        header = {
            // Header icon button should have a tooltip.
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(headerDescription) } },
                state = rememberTooltipState(),
            ) {
                IconButton(
                    modifier = Modifier.padding(start = 24.dp),
                    onClick = {
                        scope.launch {
                            if (state.targetValue == WideNavigationRailValue.Expanded) state.collapse()
                            else state.expand()
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(when {
                            state.targetValue == WideNavigationRailValue.Expanded -> Res.drawable.menu_open_24px
                            else -> Res.drawable.menu_24px
                        }),
                        contentDescription = headerDescription
                    )
                }
            }
        },
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { mediaStateDestination ->
            WideNavigationRailItem(
                railExpanded = state.targetValue == WideNavigationRailValue.Expanded,
                icon = {
                    Icon(
                        painter = painterResource(mediaStateDestination.icon),
                        contentDescription = stringResource(mediaStateDestination.iconTextResource),
                    )
                },
                label = {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = stringResource(mediaStateDestination.iconTextResource),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentMediaStateEq == mediaStateDestination.mediaStateEq,
                onClick = { navigateToTopLevelDestination(mediaStateDestination.mediaStateEq) },
            )
        }
    }
}

@Composable
fun MediaBottomNavigationBar(
    currentMediaStateEq: Media.State,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        TOP_LEVEL_DESTINATIONS.forEach { mediaStateDestination ->
            NavigationBarItem(
                selected = currentMediaStateEq == mediaStateDestination.mediaStateEq,
                onClick = { navigateToTopLevelDestination(mediaStateDestination.mediaStateEq) },
                icon = {
                    Icon(
                        painter = painterResource(mediaStateDestination.icon),
                        contentDescription = stringResource(mediaStateDestination.iconTextResource),
                    )
                },
                label = {
                    Text(
                        text = stringResource(mediaStateDestination.iconTextResource),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

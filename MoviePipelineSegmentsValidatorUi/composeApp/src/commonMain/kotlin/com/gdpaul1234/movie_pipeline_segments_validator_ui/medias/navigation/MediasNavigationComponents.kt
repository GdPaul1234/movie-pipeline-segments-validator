package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.navigation

/**
 * Inspired of Reply android jetpack compose sample
 * https://github.com/android/compose-samples/blob/main/Reply/app/src/main/java/com/example/reply/ui/navigation/ReplyNavigationComponents.kt
 */

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Avoid opening the modal drawer when there is a bottom nav bar, but always allow closing an open drawer.
    val gesturesEnabled = drawerState.isOpen || navLayoutType == MediasNavigationType.NAVIGATION_RAIL

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalNavigationDrawerContent(
                currentMediaStateEq = currentMediaStateEq,
                navigateToTopLevelDestination = navigateToTopLevelDestination,
                onDrawerClicked = {  coroutineScope.launch { drawerState.close() } }
            )
        },
    ) {
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
                            onDrawerClicked = { coroutineScope.launch { drawerState.open() } },
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
}

@Composable
fun MediaNavigationRail(
    currentMediaStateEq: Media.State,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavigationRailItem(
                selected = false,
                onClick = onDrawerClicked,
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.menu_24px),
                        contentDescription = stringResource(Res.string.navigation_drawer),
                    )
                },
            )
            Spacer(Modifier.height(8.dp)) // NavigationRailHeaderPadding
            Spacer(Modifier.height(4.dp)) // NavigationRailVerticalPadding
        }

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(items = TOP_LEVEL_DESTINATIONS, key = { it.mediaStateEq }) { mediaStateDestination ->
                NavigationRailItem(
                    selected = currentMediaStateEq == mediaStateDestination.mediaStateEq,
                    onClick = { navigateToTopLevelDestination(mediaStateDestination.mediaStateEq) },
                    icon = {
                        Icon(
                            painter = painterResource(mediaStateDestination.icon),
                            contentDescription = stringResource(mediaStateDestination.iconTextResource),
                        )
                    },
                )
            }
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

@Composable
fun ModalNavigationDrawerContent(
    currentMediaStateEq: Media.State,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    ) {
                    IconButton(
                        modifier = Modifier.size(64.dp),
                        onClick = onDrawerClicked
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.menu_open_24px),
                            contentDescription = stringResource(Res.string.close_drawer),
                        )
                    }
                }
            }

            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(TOP_LEVEL_DESTINATIONS, { it.mediaStateEq }) { mediaStateDestination ->
                    NavigationDrawerItem(
                        selected = currentMediaStateEq == mediaStateDestination.mediaStateEq,
                        label = {
                            Text(
                                text = stringResource(mediaStateDestination.iconTextResource),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(mediaStateDestination.icon),
                                contentDescription = stringResource(mediaStateDestination.iconTextResource),
                            )
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                        onClick = { navigateToTopLevelDestination(mediaStateDestination.mediaStateEq) },
                    )
                }
            }
        }
    }
}


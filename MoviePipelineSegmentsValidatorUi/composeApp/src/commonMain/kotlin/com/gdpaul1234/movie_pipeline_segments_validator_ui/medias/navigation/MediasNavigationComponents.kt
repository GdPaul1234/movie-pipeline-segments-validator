package com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.navigation

/**
 * Inspired of Reply android jetpack compose sample
 * https://github.com/android/compose-samples/blob/main/Reply/app/src/main/java/com/example/reply/ui/navigation/ReplyNavigationComponents.kt
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.util.MediasNavigationContentPosition
import com.gdpaul1234.movie_pipeline_segments_validator_ui.medias.presentation.util.MediasNavigationType
import kotlinx.coroutines.launch
import moviepipelinesegmentsvalidatorui.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openapitools.client.models.Media

private fun WindowSizeClass.isMedium() =
    isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

private fun WindowSizeClass.isLarge() =
    isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

@Composable
fun MediasNavigationWrapper(
    currentMediaStateEq: Media.State,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
    content: @Composable (MediasNavigationType) -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()

    val navLayoutType = when {
        adaptiveInfo.windowPosture.isTabletop -> MediasNavigationType.NAVIGATION_BAR
        adaptiveInfo.windowSizeClass.isLarge() -> MediasNavigationType.NAVIGATION_DRAWER
        adaptiveInfo.windowSizeClass.isMedium() -> MediasNavigationType.NAVIGATION_RAIL
        else -> MediasNavigationType.NAVIGATION_BAR
    }

    val navContentPosition = when {
        adaptiveInfo.windowSizeClass.isMedium() -> MediasNavigationContentPosition.TOP
        else -> MediasNavigationContentPosition.CENTER
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Avoid opening the modal drawer when there is a permanent drawer or a bottom nav bar,
    // but always allow closing an open drawer.
    val gesturesEnabled = drawerState.isOpen || navLayoutType == MediasNavigationType.NAVIGATION_RAIL


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalNavigationDrawerContent(
                currentMediaStateEq = currentMediaStateEq,
                navigationContentPosition = navContentPosition,
                navigateToTopLevelDestination = navigateToTopLevelDestination,
                onDrawerClicked = {  coroutineScope.launch { drawerState.close() } }
            )
        },
    ) {
        Scaffold(
            bottomBar = {
                if(navLayoutType == MediasNavigationType.NAVIGATION_BAR) {
                    MediaBottomNavigationBar(
                        currentMediaStateEq = currentMediaStateEq,
                        navigateToTopLevelDestination = navigateToTopLevelDestination,
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
            ) {
                Row {
                    when (navLayoutType) {
                        MediasNavigationType.NAVIGATION_RAIL -> MediaNavigationRail(
                            currentMediaStateEq = currentMediaStateEq,
                            navigateToTopLevelDestination = navigateToTopLevelDestination,
                            onDrawerClicked = { coroutineScope.launch { drawerState.open() } },
                        )
                        MediasNavigationType.NAVIGATION_DRAWER -> PermanentNavigationDrawerContent(
                            currentMediaStateEq = currentMediaStateEq,
                            navigationContentPosition = navContentPosition,
                            navigateToTopLevelDestination = navigateToTopLevelDestination,
                        )
                        else -> Spacer(Modifier)
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
            modifier = Modifier.layoutId(LayoutType.HEADER),
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
            FloatingActionButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.cloud_sync_24px),
                    contentDescription = stringResource(Res.string.refresh_session_content),
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(8.dp)) // NavigationRailHeaderPadding
            Spacer(Modifier.height(4.dp)) // NavigationRailVerticalPadding
        }

        Column(
            modifier = Modifier.layoutId(LayoutType.CONTENT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TOP_LEVEL_DESTINATIONS.forEach { mediaStateDestination ->
                NavigationRailItem(
                    selected = currentMediaStateEq == mediaStateDestination.mediaStateEq,
                    onClick = { navigateToTopLevelDestination(mediaStateDestination.mediaStateEq) },
                    icon = {
                        Icon(
                            painter = painterResource(mediaStateDestination.icon),
                            contentDescription = stringResource(
                                mediaStateDestination.iconTextResource,
                            ),
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
fun PermanentNavigationDrawerContent(
    currentMediaStateEq: Media.State,
    navigationContentPosition: MediasNavigationContentPosition,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
) {
    PermanentDrawerSheet(
        modifier = Modifier.sizeIn(minWidth = 200.dp, maxWidth = 300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        // TODO remove custom nav drawer content positioning when NavDrawer component supports it. ticket : b/232495216
        Layout(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp),
            content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Spacer(modifier = Modifier.height(64.dp))

                    ExtendedFloatingActionButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 40.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.cloud_sync_24px),
                            contentDescription = stringResource(Res.string.refresh_session_content),
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = stringResource(Res.string.refresh_session_content),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .layoutId(LayoutType.CONTENT)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TOP_LEVEL_DESTINATIONS.forEach { mediaStateDestination ->
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
                                    contentDescription = stringResource(mediaStateDestination.iconTextResource ),
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                            ),
                            onClick = { navigateToTopLevelDestination(mediaStateDestination.mediaStateEq) },
                        )
                    }
                }
            },
            measurePolicy = navigationMeasurePolicy(navigationContentPosition),
        )
    }
}

@Composable
fun ModalNavigationDrawerContent(
    currentMediaStateEq: Media.State,
    navigationContentPosition: MediasNavigationContentPosition,
    navigateToTopLevelDestination: (mediaStateEq: Media.State) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    ModalDrawerSheet {
        // TODO remove custom nav drawer content positioning when NavDrawer component supports it. ticket : b/232495216
        Layout(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.inverseOnSurface)
                .padding(16.dp),
            content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                        ,horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onDrawerClicked) {
                            Icon(
                                painter = painterResource(Res.drawable.menu_24px),
                                contentDescription = stringResource(Res.string.close_drawer),
                            )
                        }
                    }

                    ExtendedFloatingActionButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 40.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.cloud_sync_24px),
                            contentDescription = stringResource(Res.string.refresh_session_content),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(Res.string.refresh_session_content),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .layoutId(LayoutType.CONTENT)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TOP_LEVEL_DESTINATIONS.forEach { mediaStateDestination ->
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
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                            ),
                            onClick = { navigateToTopLevelDestination(mediaStateDestination.mediaStateEq) },
                        )
                    }
                }
            },
            measurePolicy = navigationMeasurePolicy(navigationContentPosition),
        )
    }
}

fun navigationMeasurePolicy(navigationContentPosition: MediasNavigationContentPosition): MeasurePolicy {
    return MeasurePolicy { measurables, constraints ->
        lateinit var headerMeasurable: Measurable
        lateinit var contentMeasurable: Measurable
        measurables.forEach {
            when (it.layoutId) {
                LayoutType.HEADER -> headerMeasurable = it
                LayoutType.CONTENT -> contentMeasurable = it
                else -> error("Unknown layoutId encountered!")
            }
        }

        val headerPlaceable = headerMeasurable.measure(constraints)
        val contentPlaceable = contentMeasurable.measure(
            constraints.offset(vertical = -headerPlaceable.height),
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place the header, this goes at the top
            headerPlaceable.placeRelative(0, 0)

            // Determine how much space is not taken up by the content
            val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

            val contentPlaceableY = when (navigationContentPosition) {
                // Figure out the place we want to place the content, with respect to the
                // parent (ignoring the header for now)
                MediasNavigationContentPosition.TOP -> 0
                MediasNavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
            }
                // And finally, make sure we don't overlap with the header.
                .coerceAtLeast(headerPlaceable.height)

            contentPlaceable.placeRelative(0, contentPlaceableY)
        }
    }
}

enum class LayoutType {
    HEADER,
    CONTENT,
}

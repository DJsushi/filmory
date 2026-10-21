package dev.gaens.filmory.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.bottomBarContentPadding
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.LocalReducedMotion
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.SquircleShape
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryCard
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryTile
import dev.gaens.filmory.core.presentation.designsystem.components.GrowingProgressBar
import dev.gaens.filmory.core.presentation.designsystem.components.HeroHeader
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.SectionAction
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.components.posterGradient
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.core.presentation.designsystem.spanParentPadding
import dev.gaens.filmory.core.presentation.ui.formatMonthYear
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.feature.settings.domain.HomeBlock
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_done
import dev.gaens.filmory.resources.app_name
import dev.gaens.filmory.resources.home_drag_handle
import dev.gaens.filmory.resources.home_edit_layout
import dev.gaens.filmory.resources.home_edit_layout_hint
import dev.gaens.filmory.resources.home_move_block_down
import dev.gaens.filmory.resources.home_move_block_up
import dev.gaens.filmory.resources.home_reset_layout
import dev.gaens.filmory.resources.ic_drag_handle
import dev.gaens.filmory.resources.ic_tune
import dev.gaens.filmory.resources.home_calendar
import dev.gaens.filmory.resources.home_collections
import dev.gaens.filmory.resources.home_continue_watching
import dev.gaens.filmory.resources.home_greeting_afternoon
import dev.gaens.filmory.resources.home_greeting_evening
import dev.gaens.filmory.resources.home_greeting_morning
import dev.gaens.filmory.resources.home_library
import dev.gaens.filmory.resources.home_more
import dev.gaens.filmory.resources.home_not_started
import dev.gaens.filmory.resources.home_season_episode
import dev.gaens.filmory.resources.home_subtitle
import dev.gaens.filmory.resources.home_subtitle_empty
import dev.gaens.filmory.resources.home_watch_later
import dev.gaens.filmory.resources.home_watch_later_add
import dev.gaens.filmory.resources.home_wishlist
import dev.gaens.filmory.resources.action_edit
import dev.gaens.filmory.resources.ic_add
import dev.gaens.filmory.resources.ic_bookmark
import dev.gaens.filmory.resources.ic_calendar
import dev.gaens.filmory.resources.ic_flag
import dev.gaens.filmory.resources.ic_library
import dev.gaens.filmory.resources.ic_pause
import dev.gaens.filmory.resources.ic_play
import dev.gaens.filmory.resources.status_dropped
import dev.gaens.filmory.resources.status_on_hold
import dev.gaens.filmory.resources.title_count
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

@Composable
fun HomeScreenRoot(
    onTitleClick: (String) -> Unit,
    onLibraryClick: (LibraryPreset, String?) -> Unit,
    onCalendarClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HomeAction.OnTitleClick -> onTitleClick(action.titleId)
                is HomeAction.OnLibraryClick -> onLibraryClick(action.preset, action.collectionId)
                HomeAction.OnCalendarClick -> onCalendarClick()
                HomeAction.OnMoreCollectionsClick -> onCollectionsClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
        val block = HomeBlock.entries.find { it.name == from.key } ?: return@rememberReorderableLazyGridState
        val target = HomeBlock.entries.find { it.name == to.key } ?: return@rememberReorderableLazyGridState
        onAction(HomeAction.OnMoveBlock(block, target))
        haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    BackHandler(enabled = state.isEditingLayout) { onAction(HomeAction.OnEditLayoutDone) }
    val spans = homeSpans(state.layout)
    val bottomPadding = bottomBarContentPadding().calculateBottomPadding()
    LazyVerticalGrid(
        columns = GridCells.Fixed(HOME_GRID_COLUMNS),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = HERO_KEY, span = { GridItemSpan(maxLineSpan) }) {
            HomeHero(state, onAction, Modifier.spanParentPadding())
        }
        itemsIndexed(state.layout, key = { _, block -> block.name }, span = { _, block -> GridItemSpan(spans.getValue(block)) }) { index, block ->
            ReorderableItem(reorderableState, key = block.name) { isDragging ->
                val dragStarted = { _: Offset -> haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) }
                val dragStopped = {
                    haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
                    onAction(HomeAction.OnLayoutDropped)
                }
                HomeBlockItem(
                    block = block,
                    isWide = spans.getValue(block) == HOME_GRID_COLUMNS,
                    state = state,
                    riseIndex = BLOCKS_RISE_START + index,
                    isDragging = isDragging,
                    wholeBlockDrag = Modifier.longPressDraggableHandle(
                        enabled = state.isEditingLayout,
                        onDragStarted = dragStarted,
                        onDragStopped = dragStopped,
                    ),
                    handleDrag = Modifier.draggableHandle(onDragStarted = dragStarted, onDragStopped = dragStopped),
                    onAction = onAction,
                )
            }
        }
    }
    if (state.isEditingWatchLater) {
        WatchLaterEditor(state.watchLater, onAction)
    }
}

private const val HERO_KEY = "hero"
private const val BLOCKS_RISE_START = 3
private const val LIFTED_SCALE = 1.04f

/** One block of the Home grid. In layout edit mode it cannot be opened and can be dragged. */
@Composable
private fun HomeBlockItem(
    block: HomeBlock,
    isWide: Boolean,
    state: HomeState,
    riseIndex: Int,
    isDragging: Boolean,
    wholeBlockDrag: Modifier,
    handleDrag: Modifier,
    onAction: (HomeAction) -> Unit,
) {
    val isEditing = state.isEditingLayout
    val moveUp = stringResource(Res.string.home_move_block_up)
    val moveDown = stringResource(Res.string.home_move_block_down)
    val scale by animateFloatAsState(if (isDragging && !LocalReducedMotion.current) LIFTED_SCALE else 1f)
    val dragHandle: @Composable () -> Unit = {
        Box(handleDrag.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(Res.drawable.ic_drag_handle),
                contentDescription = stringResource(Res.string.home_drag_handle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    val itemModifier = Modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(if (isEditing) wholeBlockDrag else Modifier)
        .semantics(mergeDescendants = isEditing) {
            if (isEditing) {
                customActions = listOf(
                    CustomAccessibilityAction(moveUp) { onAction(HomeAction.OnNudgeBlock(block, -1)); true },
                    CustomAccessibilityAction(moveDown) { onAction(HomeAction.OnNudgeBlock(block, 1)); true },
                )
            }
        }
    when (block) {
        HomeBlock.WATCH_LATER -> WatchLaterSection(state, itemModifier.spanParentPadding().rise(riseIndex), dragHandle, onAction)
        HomeBlock.COLLECTIONS ->
            CollectionsSection(state, itemModifier.spanParentPadding().rise(riseIndex), dragHandle, onAction)
        else -> HomeTile(block, state, isWide, itemModifier.padding(top = 14.dp).rise(riseIndex), onAction)
    }
}

@Composable
private fun HomeTile(
    block: HomeBlock,
    state: HomeState,
    isWide: Boolean,
    modifier: Modifier,
    onAction: (HomeAction) -> Unit,
) {
    fun open(action: HomeAction): (() -> Unit)? = if (state.isEditingLayout) null else ({ onAction(action) })
    val tile = when (block) {
        HomeBlock.WISHLIST -> TileContent(
            stringResource(Res.string.home_wishlist),
            pluralStringResource(Res.plurals.title_count, state.wishlistCount, state.wishlistCount),
            Res.drawable.ic_bookmark,
            open(HomeAction.OnLibraryClick(LibraryPreset.WISHLIST)),
        )
        HomeBlock.CALENDAR -> TileContent(
            stringResource(Res.string.home_calendar),
            state.today?.formatMonthYear().orEmpty(),
            Res.drawable.ic_calendar,
            open(HomeAction.OnCalendarClick),
        )
        HomeBlock.ON_HOLD -> TileContent(
            stringResource(Res.string.status_on_hold),
            pluralStringResource(Res.plurals.title_count, state.onHoldCount, state.onHoldCount),
            Res.drawable.ic_pause,
            open(HomeAction.OnLibraryClick(LibraryPreset.ON_HOLD)),
        )
        HomeBlock.DROPPED -> TileContent(
            stringResource(Res.string.status_dropped),
            pluralStringResource(Res.plurals.title_count, state.droppedCount, state.droppedCount),
            Res.drawable.ic_flag,
            open(HomeAction.OnLibraryClick(LibraryPreset.DROPPED)),
        )
        else -> TileContent(
            stringResource(Res.string.home_library),
            pluralStringResource(Res.plurals.title_count, state.libraryCount, state.libraryCount),
            Res.drawable.ic_library,
            open(HomeAction.OnLibraryClick(LibraryPreset.ALL)),
        )
    }
    FilmoryTile(
        title = tile.title,
        subtitle = tile.subtitle,
        icon = if (state.isEditingLayout) Res.drawable.ic_drag_handle else tile.icon,
        onClick = tile.onClick,
        isWide = isWide,
        modifier = modifier.fillMaxWidth(),
    )
}

private class TileContent(
    val title: String,
    val subtitle: String,
    val icon: DrawableResource,
    val onClick: (() -> Unit)?,
)

@Composable
private fun HomeHero(state: HomeState, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { state.continueWatching.size }
    val shown = state.continueWatching.getOrNull(pagerState.currentPage)
    val posterColor = if (state.tintHeader && shown != null) rememberPosterColor(shown.titleId, shown.posterUrl) else null
    val base = FilmoryTheme.colors.hero
    val target = posterColor?.let { headerTint(it, base, FilmoryTheme.colors.onHero) } ?: base
    val container by animateColorAsState(target, if (LocalReducedMotion.current) snap() else tween(HEADER_TINT_MILLIS))
    HeroHeader(modifier, containerColor = container, horizontalPadding = 0.dp) {
        val inset = Modifier.padding(horizontal = ScreenPadding)
        FilmoryAppBar(
            modifier = inset,
            title = stringResource(Res.string.app_name),
            actions = {
                if (state.isEditingLayout) {
                    val colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current)
                    TextButton(onClick = { onAction(HomeAction.OnResetLayout) }, colors = colors) {
                        Text(stringResource(Res.string.home_reset_layout))
                    }
                    TextButton(onClick = { onAction(HomeAction.OnEditLayoutDone) }, colors = colors) {
                        Text(stringResource(Res.string.action_done))
                    }
                } else {
                    FilmoryIconButton(
                        icon = Res.drawable.ic_tune,
                        contentDescription = stringResource(Res.string.home_edit_layout),
                        onClick = { onAction(HomeAction.OnEditLayoutClick) },
                    )
                }
            },
        )
        Text(
            text = stringResource(state.dayPeriod.greeting),
            style = MaterialTheme.typography.headlineLarge,
            modifier = inset.padding(top = 8.dp).rise(0),
        )
        val subtitle = when {
            state.isEditingLayout -> Res.string.home_edit_layout_hint
            state.libraryCount == 0 -> Res.string.home_subtitle_empty
            else -> Res.string.home_subtitle
        }
        Text(
            text = stringResource(subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = inset.padding(top = 4.dp).rise(1),
        )
        val isEditing = state.isEditingLayout
        when (state.continueWatching.size) {
            0 -> Unit
            1 -> ContinueWatchingCard(
                item = state.continueWatching.first(),
                isEnabled = !isEditing,
                onAction = onAction,
                modifier = inset.padding(top = 22.dp).rise(2),
            )
            else -> HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                // The next card shows a sliver past the end, so the list is seen to go on.
                contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding + NEXT_CARD_PEEK),
                modifier = Modifier.padding(top = 22.dp).rise(2),
            ) { page ->
                ContinueWatchingCard(state.continueWatching[page], isEnabled = !isEditing, onAction = onAction)
            }
        }
    }
}

private const val HEADER_TINT_MILLIS = 500
private val NEXT_CARD_PEEK = 16.dp

private val DayPeriod.greeting
    get() = when (this) {
        DayPeriod.MORNING -> Res.string.home_greeting_morning
        DayPeriod.AFTERNOON -> Res.string.home_greeting_afternoon
        DayPeriod.EVENING -> Res.string.home_greeting_evening
    }

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingUi,
    isEnabled: Boolean,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilmoryCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        onClick = if (isEnabled) ({ onAction(HomeAction.OnTitleClick(item.titleId)) }) else null,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PosterArt(item.titleId, item.name, item.posterUrl, Modifier.width(64.dp), showName = false)
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.home_continue_watching),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = item.progressLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.progress != null) {
                    GrowingProgressBar(item.progress, Modifier.padding(top = 8.dp))
                }
            }
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(Res.drawable.ic_play), null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun ContinueWatchingUi.progressLabel(): String = when {
    seasonNumber != null && episodeNumber != null ->
        stringResource(Res.string.home_season_episode, seasonNumber, episodeNumber)
    type == TitleType.MOVIE -> type.label()
    else -> stringResource(Res.string.home_not_started)
}

private val WatchLaterSlotSize = 76.dp

@Composable
private fun WatchLaterSection(
    state: HomeState,
    modifier: Modifier,
    dragHandle: @Composable () -> Unit,
    onAction: (HomeAction) -> Unit,
) {
    val isEditing = state.isEditingLayout
    Column(modifier) {
        SectionHeader(
            title = stringResource(Res.string.home_watch_later),
            action = {
                if (isEditing) {
                    dragHandle()
                } else {
                    SectionAction(stringResource(Res.string.action_edit), { onAction(HomeAction.OnEditWatchLaterClick) })
                }
            },
        )
        LazyRow(
            userScrollEnabled = !isEditing,
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(state.watchLater, key = { it.id }) { title ->
                PosterArt(
                    seed = title.id,
                    name = title.name,
                    posterUrl = title.posterUrl,
                    shape = SquircleShape,
                    aspectRatio = null,
                    showName = false,
                    modifier = Modifier
                        .size(WatchLaterSlotSize)
                        .softShadow(SquircleShape)
                        .clickable(enabled = !isEditing, onClickLabel = title.name, role = Role.Button) {
                            onAction(HomeAction.OnTitleClick(title.id))
                        },
                )
            }
            item(key = "add") {
                AddWatchLaterSlot(isEnabled = !isEditing) { onAction(HomeAction.OnLibraryClick(LibraryPreset.ALL)) }
            }
        }
    }
}

@Composable
private fun AddWatchLaterSlot(isEnabled: Boolean, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .size(WatchLaterSlotSize)
            .clip(SquircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawOutline(
                    outline = SquircleShape.createOutline(size, layoutDirection, this),
                    color = outline,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                    ),
                )
            }
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = stringResource(Res.string.home_watch_later_add),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CollectionsSection(
    state: HomeState,
    modifier: Modifier,
    dragHandle: @Composable () -> Unit,
    onAction: (HomeAction) -> Unit,
) {
    val isEditing = state.isEditingLayout
    Column(modifier) {
        SectionHeader(
            title = stringResource(Res.string.home_collections),
            action = {
                if (isEditing) {
                    dragHandle()
                } else {
                    SectionAction(stringResource(Res.string.home_more), { onAction(HomeAction.OnMoreCollectionsClick) })
                }
            },
        )
        LazyRow(
            userScrollEnabled = !isEditing,
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(state.collections, key = { it.id }) { collection ->
                CollectionCard(collection, isEnabled = !isEditing) {
                    onAction(HomeAction.OnLibraryClick(LibraryPreset.COLLECTION, collection.id))
                }
            }
        }
    }
}

@Composable
private fun CollectionCard(collection: CollectionUi, isEnabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .size(width = 148.dp, height = 108.dp)
            .softShadow(shape)
            .clip(shape)
            .background(posterGradient(collection.id))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(collection.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
        Text(
            text = pluralStringResource(Res.plurals.title_count, collection.titleCount, collection.titleCount),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.92f),
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    FilmoryTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            HomeScreen(
                state = HomeState(
                    continueWatching = listOf(
                        ContinueWatchingUi("1", "Severance", null, TitleType.SERIES, 2, 4, 0.3f),
                    ),
                    watchLater = listOf(TitleThumbUi("2", "Arrival", null), TitleThumbUi("3", "Past Lives", null)),
                    wishlistCount = 12,
                    collections = listOf(CollectionUi("favorites", "Favorites", 8)),
                    onHoldCount = 3,
                    droppedCount = 2,
                    libraryCount = 128,
                ),
                onAction = {},
            )
        }
    }
}

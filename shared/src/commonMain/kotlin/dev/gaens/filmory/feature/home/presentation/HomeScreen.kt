package dev.gaens.filmory.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.bottomBarContentPadding
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.SquircleShape
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryCard
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryTile
import dev.gaens.filmory.core.presentation.designsystem.components.GrowingProgressBar
import dev.gaens.filmory.core.presentation.designsystem.components.HeroHeader
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.SectionAction
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.components.posterGradient
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.core.presentation.ui.formatMonthYear
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.app_name
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = bottomBarContentPadding(),
    ) {
        item(key = "hero") { HomeHero(state, onAction) }
        item(key = "watchLater") { WatchLaterSection(state, onAction) }
        item(key = "listTiles") {
            TileRow(Modifier.padding(top = 22.dp)) {
                FilmoryTile(
                    title = stringResource(Res.string.home_wishlist),
                    subtitle = pluralStringResource(Res.plurals.title_count, state.wishlistCount, state.wishlistCount),
                    icon = Res.drawable.ic_bookmark,
                    onClick = { onAction(HomeAction.OnLibraryClick(LibraryPreset.WISHLIST)) },
                    modifier = Modifier.weight(1f).rise(4),
                )
                FilmoryTile(
                    title = stringResource(Res.string.home_calendar),
                    subtitle = state.today?.formatMonthYear().orEmpty(),
                    icon = Res.drawable.ic_calendar,
                    onClick = { onAction(HomeAction.OnCalendarClick) },
                    modifier = Modifier.weight(1f).rise(5),
                )
            }
        }
        item(key = "collections") { CollectionsSection(state.collections, onAction) }
        item(key = "statusTiles") { StatusTiles(state, onAction) }
    }
    if (state.isEditingWatchLater) {
        WatchLaterEditor(state.watchLater, onAction)
    }
}

@Composable
private fun HomeHero(state: HomeState, onAction: (HomeAction) -> Unit) {
    HeroHeader {
        FilmoryAppBar(title = stringResource(Res.string.app_name))
        Text(
            text = stringResource(state.dayPeriod.greeting),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp).rise(0),
        )
        Text(
            text = stringResource(if (state.libraryCount == 0) Res.string.home_subtitle_empty else Res.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp).rise(1),
        )
        when (state.continueWatching.size) {
            0 -> Unit
            1 -> ContinueWatchingCard(state.continueWatching.first(), onAction, Modifier.padding(top = 22.dp).rise(2))
            else -> {
                val pagerState = rememberPagerState { state.continueWatching.size }
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 12.dp,
                    modifier = Modifier.padding(top = 22.dp).rise(2),
                ) { page ->
                    ContinueWatchingCard(state.continueWatching[page], onAction)
                }
            }
        }
    }
}

private val DayPeriod.greeting
    get() = when (this) {
        DayPeriod.MORNING -> Res.string.home_greeting_morning
        DayPeriod.AFTERNOON -> Res.string.home_greeting_afternoon
        DayPeriod.EVENING -> Res.string.home_greeting_evening
    }

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingUi,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilmoryCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        onClick = { onAction(HomeAction.OnTitleClick(item.titleId)) },
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
private fun WatchLaterSection(state: HomeState, onAction: (HomeAction) -> Unit) {
    SectionHeader(
        title = stringResource(Res.string.home_watch_later),
        action = { SectionAction(stringResource(Res.string.action_edit), { onAction(HomeAction.OnEditWatchLaterClick) }) },
    )
    LazyRow(
        modifier = Modifier.rise(3),
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
                    .clickable(onClickLabel = title.name, role = Role.Button) {
                        onAction(HomeAction.OnTitleClick(title.id))
                    },
            )
        }
        item(key = "add") { AddWatchLaterSlot { onAction(HomeAction.OnLibraryClick(LibraryPreset.ALL)) } }
    }
}

@Composable
private fun AddWatchLaterSlot(onClick: () -> Unit) {
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
            .clickable(role = Role.Button, onClick = onClick),
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
private fun CollectionsSection(collections: List<CollectionUi>, onAction: (HomeAction) -> Unit) {
    SectionHeader(
        title = stringResource(Res.string.home_collections),
        action = { SectionAction(stringResource(Res.string.home_more), { onAction(HomeAction.OnMoreCollectionsClick) }) },
    )
    LazyRow(
        modifier = Modifier.rise(6),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(collections, key = { it.id }) { collection ->
            CollectionCard(collection) { onAction(HomeAction.OnLibraryClick(LibraryPreset.COLLECTION, collection.id)) }
        }
    }
}

@Composable
private fun CollectionCard(collection: CollectionUi, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .size(width = 148.dp, height = 108.dp)
            .softShadow(shape)
            .clip(shape)
            .background(posterGradient(collection.id))
            .clickable(onClick = onClick)
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

@Composable
private fun StatusTiles(state: HomeState, onAction: (HomeAction) -> Unit) {
    Column(Modifier.padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TileRow {
            FilmoryTile(
                title = stringResource(Res.string.status_on_hold),
                subtitle = pluralStringResource(Res.plurals.title_count, state.onHoldCount, state.onHoldCount),
                icon = Res.drawable.ic_pause,
                onClick = { onAction(HomeAction.OnLibraryClick(LibraryPreset.ON_HOLD)) },
                modifier = Modifier.weight(1f).rise(7),
            )
            FilmoryTile(
                title = stringResource(Res.string.status_dropped),
                subtitle = pluralStringResource(Res.plurals.title_count, state.droppedCount, state.droppedCount),
                icon = Res.drawable.ic_flag,
                onClick = { onAction(HomeAction.OnLibraryClick(LibraryPreset.DROPPED)) },
                modifier = Modifier.weight(1f).rise(8),
            )
        }
        FilmoryTile(
            title = stringResource(Res.string.home_library),
            subtitle = pluralStringResource(Res.plurals.title_count, state.libraryCount, state.libraryCount),
            icon = Res.drawable.ic_library,
            onClick = { onAction(HomeAction.OnLibraryClick(LibraryPreset.ALL)) },
            isWide = true,
            modifier = Modifier.padding(horizontal = ScreenPadding).fillMaxWidth().rise(9),
        )
    }
}

@Composable
private fun TileRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
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

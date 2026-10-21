package dev.gaens.filmory.feature.search.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryBadge
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.TypeBadge
import dev.gaens.filmory.core.presentation.designsystem.components.statusColors
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_back
import dev.gaens.filmory.resources.ic_add
import dev.gaens.filmory.resources.ic_back
import dev.gaens.filmory.resources.ic_close
import dev.gaens.filmory.resources.ic_search
import dev.gaens.filmory.resources.search_add_manually
import dev.gaens.filmory.resources.search_clear
import dev.gaens.filmory.resources.search_hint
import dev.gaens.filmory.resources.search_in_library
import dev.gaens.filmory.resources.search_no_results
import dev.gaens.filmory.resources.search_prompt
import dev.gaens.filmory.resources.search_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreenRoot(
    onBackClick: () -> Unit,
    onTitleAdded: (String) -> Unit,
    onAddManuallyClick: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SearchEvent.OpenTitle -> onTitleAdded(event.titleId)
            is SearchEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        SearchScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    SearchAction.OnBackClick -> onBackClick()
                    SearchAction.OnAddManuallyClick -> onAddManuallyClick()
                    else -> Unit
                }
                viewModel.onAction(action)
            },
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().imePadding())
    }
}

@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        FilmoryAppBar(
            title = stringResource(Res.string.search_title),
            modifier = Modifier.padding(horizontal = 12.dp),
            navigationIcon = {
                FilmoryIconButton(Res.drawable.ic_back, stringResource(Res.string.action_back), {
                    onAction(SearchAction.OnBackClick)
                })
            },
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(SearchAction.OnQueryChange(it)) },
            placeholder = { Text(stringResource(Res.string.search_hint)) },
            leadingIcon = { Icon(painterResource(Res.drawable.ic_search), null) },
            trailingIcon = if (state.query.isEmpty()) null else {
                {
                    FilmoryIconButton(Res.drawable.ic_close, stringResource(Res.string.search_clear), {
                        onAction(SearchAction.OnQueryChange(""))
                    })
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 8.dp).focusRequester(focusRequester),
        )
        if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = ScreenPadding))
        val message = when {
            state.error != null -> state.error.asString()
            state.query.trim().length < 2 -> stringResource(Res.string.search_prompt)
            state.results.isEmpty() && !state.isLoading -> stringResource(Res.string.search_no_results)
            else -> null
        }
        LazyColumn(Modifier.weight(1f)) {
            if (message != null) {
                item(key = "message") {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 16.dp),
                    )
                }
            }
            item(key = "manual") {
                FilledTonalButton(
                    onClick = { onAction(SearchAction.OnAddManuallyClick) },
                    modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp),
                ) {
                    Icon(painterResource(Res.drawable.ic_add), null, Modifier.size(18.dp))
                    Text(stringResource(Res.string.search_add_manually), Modifier.padding(start = 8.dp))
                }
            }
            items(state.results, key = { "${it.result.type}-${it.result.tmdbId}" }) { item ->
                SearchResultRow(item, isAdding = state.addingTmdbId == item.result.tmdbId) {
                    onAction(SearchAction.OnResultClick(item.result))
                }
            }
            item(key = "attribution") { TmdbAttribution(Modifier.padding(ScreenPadding)) }
        }
    }
}

@Composable
private fun SearchResultRow(item: SearchResultUi, isAdding: Boolean, onClick: () -> Unit) {
    val result = item.result
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAdding, onClick = onClick)
            .padding(horizontal = ScreenPadding, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PosterArt(
            seed = "${result.type}-${result.tmdbId}",
            name = result.name,
            posterUrl = result.posterUrl,
            showName = false,
            modifier = Modifier.width(56.dp).softShadow(MaterialTheme.shapes.small),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(result.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TypeBadge(result.type.label())
                if (result.year != null) {
                    Text(result.year.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.isInLibrary) {
                    FilmoryBadge(stringResource(Res.string.search_in_library), statusColors(TitleStatus.WATCHED))
                }
            }
            if (result.overview != null) {
                Text(
                    text = result.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (isAdding) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Preview
@Composable
private fun SearchScreenPreview() {
    FilmoryTheme { SearchScreen(SearchState(), onAction = {}) }
}

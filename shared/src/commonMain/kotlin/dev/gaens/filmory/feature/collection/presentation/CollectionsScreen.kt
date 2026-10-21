package dev.gaens.filmory.feature.collection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ScreenHeader
import dev.gaens.filmory.core.presentation.designsystem.components.TextInputDialog
import dev.gaens.filmory.core.presentation.designsystem.components.posterGradient
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.core.presentation.designsystem.spanParentPadding
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.collection_name
import dev.gaens.filmory.resources.collections_add
import dev.gaens.filmory.resources.collections_default_name
import dev.gaens.filmory.resources.collections_title
import dev.gaens.filmory.resources.ic_add
import dev.gaens.filmory.resources.title_count
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionsScreenRoot(
    onBackClick: () -> Unit,
    onCollectionClick: (String) -> Unit,
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectionsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                CollectionsAction.OnBackClick -> onBackClick()
                is CollectionsAction.OnCollectionClick -> onCollectionClick(action.collectionId)
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun CollectionsScreen(
    state: CollectionsState,
    onAction: (CollectionsAction) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.spanParentPadding().padding(bottom = 12.dp)) {
                ScreenHeader(
                    title = stringResource(Res.string.collections_title),
                    subtitle = null,
                    onBackClick = { onAction(CollectionsAction.OnBackClick) },
                )
            }
        }
        itemsIndexed(state.collections, key = { _, it -> it.id }) { index, collection ->
            Column(
                modifier = Modifier
                    .rise(index + 1)
                    .height(120.dp)
                    .softShadow(CardShape)
                    .clip(CardShape)
                    .background(posterGradient(collection.id))
                    .clickable { onAction(CollectionsAction.OnCollectionClick(collection.id)) }
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = if (collection.isDefault) stringResource(Res.string.collections_default_name) else collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                )
                Text(
                    text = pluralStringResource(Res.plurals.title_count, collection.titleCount, collection.titleCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
        }
        item(key = "add") { AddCollectionTile { onAction(CollectionsAction.OnAddClick) } }
    }
    if (state.isAddDialogOpen) {
        TextInputDialog(
            title = stringResource(Res.string.collections_add),
            label = stringResource(Res.string.collection_name),
            onConfirm = { onAction(CollectionsAction.OnAddConfirm(it)) },
            onDismiss = { onAction(CollectionsAction.OnAddDismiss) },
        )
    }
}

@Composable
private fun AddCollectionTile(onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .height(120.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawOutline(
                    outline = CardShape.createOutline(size, layoutDirection, this),
                    color = outline,
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(Res.drawable.ic_add), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(Res.string.collections_add),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun CollectionsScreenPreview() {
    FilmoryTheme {
        CollectionsScreen(
            state = CollectionsState(
                listOf(CollectionCardUi("favorites", "Favorites", true, 8), CollectionCardUi("2", "Rainy day", false, 5)),
            ),
            onAction = {},
        )
    }
}

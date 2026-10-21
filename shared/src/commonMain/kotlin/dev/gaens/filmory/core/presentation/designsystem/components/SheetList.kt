package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Whether a list inside a bottom sheet may scroll by touch. A touch-scrollable list takes every drag that
 * starts on it, even one that could not scroll anywhere, so the sheet could not be dragged by its content.
 * So the list scrolls only while the sheet is fully open and the content really overflows; in every other
 * case the sheet gets the drag itself. Pass the result to the list's `userScrollEnabled`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSheetListScrollEnabled(sheetState: SheetState, listState: LazyListState): Boolean {
    val isOverflowing by remember(listState) { derivedStateOf { listState.canScrollForward || listState.canScrollBackward } }
    return sheetState.currentValue == SheetValue.Expanded && isOverflowing
}

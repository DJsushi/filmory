package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Side margin of every screen. */
val ScreenPadding = 12.dp

/** Space kept free at the end of tab screens so content clears the bottom bar. */
val BottomBarClearance = 104.dp

/** Distance of the floating action button from the bottom on tab screens. */
val FabBottomOffset = 100.dp

/** Lets a full-width item in a padded lazy grid span the padding too, so its own margins line up. */
fun Modifier.spanParentPadding(horizontal: Dp = ScreenPadding): Modifier = layout { measurable, constraints ->
    val extra = (horizontal * 2).roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra, minWidth = 0))
    layout(placeable.width - extra, placeable.height) { placeable.place(-extra / 2, 0) }
}

/** Bottom content padding for tab screens: the bar plus the system navigation bar. */
@Composable
fun bottomBarContentPadding(): PaddingValues =
    PaddingValues(bottom = BottomBarClearance + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())

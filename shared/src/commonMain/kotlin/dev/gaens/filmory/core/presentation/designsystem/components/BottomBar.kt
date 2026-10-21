package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.EmphasizedEasing
import dev.gaens.filmory.core.presentation.designsystem.LocalReducedMotion
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class BottomBarItem(val label: String, val icon: DrawableResource)

private val BarShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

/** Bookmory's rounded bottom bar. The active item is a solid circle that pops in. */
@Composable
fun FilmoryBottomBar(
    items: List<BottomBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(BarShape)
            .background(MaterialTheme.colorScheme.surfaceContainer, BarShape)
            .navigationBarsPadding()
            .height(84.dp)
            .padding(horizontal = 12.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            BottomBarEntry(
                item = item,
                isSelected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomBarEntry(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.selectable(
            selected = isSelected,
            interactionSource = remember { MutableInteractionSource() },
            // The circular active item is the feedback; a ripple behind it looks like a stray splash.
            indication = null,
            role = Role.Tab,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (isSelected) {
            SelectedIndicator(item)
        } else {
            Box(Modifier.size(width = 64.dp, height = 40.dp), contentAlignment = Alignment.Center) {
                Icon(painterResource(item.icon), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SelectedIndicator(item: BottomBarItem) {
    val reducedMotion = LocalReducedMotion.current
    val progress = remember { Animatable(if (reducedMotion) 1f else 0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(400, easing = EmphasizedEasing)) }
    Box(
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                val scale = 0.5f + 0.5f * progress.value
                scaleX = scale
                scaleY = scale
                alpha = 0.4f + 0.6f * progress.value
            }
            .softShadow(CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(item.icon), item.label, tint = MaterialTheme.colorScheme.onPrimary)
    }
}

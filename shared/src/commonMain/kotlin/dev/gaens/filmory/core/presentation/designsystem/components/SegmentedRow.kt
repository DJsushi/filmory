package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.EmphasizedEasing
import dev.gaens.filmory.core.presentation.designsystem.LocalReducedMotion

private val RowShape = RoundedCornerShape(20.dp)

private const val SLIDE_MILLIS = 300

/**
 * The mockup's outlined segmented row. Labels may wrap to two lines so five options fit a phone.
 * The highlight is one block that slides to the selected segment, and jumps under reduced motion.
 */
@Composable
fun FilmorySegmentedRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlight = MaterialTheme.colorScheme.primary
    val position by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = if (LocalReducedMotion.current) snap() else tween(SLIDE_MILLIS, easing = EmphasizedEasing),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RowShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, RowShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                val segmentWidth = size.width / options.size
                drawRect(highlight, Offset(position * segmentWidth, 0f), Size(segmentWidth, size.height))
            }
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, label ->
            if (index > 0) VerticalDivider(color = MaterialTheme.colorScheme.outline)
            val isSelected = index == selectedIndex
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                animationSpec = if (LocalReducedMotion.current) snap() else tween(SLIDE_MILLIS),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .defaultMinSize(minHeight = 40.dp)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(index) })
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color = textColor,
                )
            }
        }
    }
}

package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.popIn
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.ic_add
import org.jetbrains.compose.resources.painterResource

private const val FAB_POP_DELAY_MILLIS = 250L

@Composable
fun FilmoryFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(painterResource(Res.drawable.ic_add), null) },
        text = { Text(text) },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.popIn(FAB_POP_DELAY_MILLIS),
    )
}

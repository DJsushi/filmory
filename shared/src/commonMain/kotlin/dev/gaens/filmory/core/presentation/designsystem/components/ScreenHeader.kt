package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_back
import dev.gaens.filmory.resources.ic_back
import org.jetbrains.compose.resources.stringResource

/** The plain top area of pushed screens: back button, actions, big headline and a subtitle. */
@Composable
fun ScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
        FilmoryAppBar(
            navigationIcon = {
                FilmoryIconButton(Res.drawable.ic_back, stringResource(Res.string.action_back), onBackClick)
            },
            actions = actions,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp).rise(0),
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp).rise(1),
            )
        }
    }
}

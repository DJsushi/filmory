package dev.gaens.filmory.feature.search.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.tmdb_logo
import dev.gaens.filmory.resources.tmdb_notice
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The logo and notice TMDB requires wherever its data is used. */
@Composable
fun TmdbAttribution(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(Res.drawable.tmdb_logo), contentDescription = stringResource(Res.string.tmdb_logo))
        Text(
            text = stringResource(Res.string.tmdb_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

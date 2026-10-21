package dev.gaens.filmory.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.home_greeting
import dev.gaens.filmory.resources.home_tagline
import org.jetbrains.compose.resources.stringResource

private val HeroCurve = RoundedCornerShape(bottomStart = 44.dp, bottomEnd = 44.dp)

// Stands in until the real Home screen lands in milestone 3.
@Composable
fun HomePlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FilmoryTheme.colors.hero, HeroCurve)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_greeting),
                style = MaterialTheme.typography.headlineLarge,
                color = FilmoryTheme.colors.onHero,
            )
            Text(
                text = stringResource(Res.string.home_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = FilmoryTheme.colors.onHero,
            )
        }
    }
}

@Preview
@Composable
private fun HomePlaceholderPreview() {
    FilmoryTheme { HomePlaceholder() }
}

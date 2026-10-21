package dev.gaens.filmory.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.feature.search.presentation.TmdbAttribution
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.bottomBarContentPadding
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryCard
import dev.gaens.filmory.core.presentation.designsystem.components.FilmorySegmentedRow
import dev.gaens.filmory.core.presentation.designsystem.components.HeroHeader
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.components.SettingSwitch
import dev.gaens.filmory.core.presentation.designsystem.isDynamicColorAvailable
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.feature.settings.domain.ThemeMode
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.app_name
import dev.gaens.filmory.resources.note_count
import dev.gaens.filmory.resources.profile_subtitle
import dev.gaens.filmory.resources.profile_title
import dev.gaens.filmory.resources.settings_about
import dev.gaens.filmory.resources.settings_appearance
import dev.gaens.filmory.resources.settings_dynamic_color
import dev.gaens.filmory.resources.settings_dynamic_color_unavailable
import dev.gaens.filmory.resources.settings_local_first
import dev.gaens.filmory.resources.settings_theme
import dev.gaens.filmory.resources.settings_theme_dark
import dev.gaens.filmory.resources.settings_theme_light
import dev.gaens.filmory.resources.settings_theme_system
import dev.gaens.filmory.resources.title_count
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenRoot(
    dataSection: @Composable (onMessage: suspend (UiText) -> Unit) -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    Box(Modifier.fillMaxSize()) {
        SettingsScreen(
            state = state,
            onAction = viewModel::onAction,
            dataSection = { dataSection { snackbarHostState.showSnackbar(it.asStringAsync()) } },
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 96.dp))
    }
}

/** The Profile tab: a summary and the app settings. [dataSection] comes from the backup feature. */
@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    dataSection: @Composable () -> Unit = {},
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottomBarContentPadding())) {
        HeroHeader {
            FilmoryAppBar(title = stringResource(Res.string.app_name))
            Text(
                text = stringResource(Res.string.profile_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 8.dp).rise(0),
            )
            Text(
                text = stringResource(
                    Res.string.profile_subtitle,
                    pluralStringResource(Res.plurals.title_count, state.titleCount, state.titleCount),
                    state.watchedCount.toString(),
                    pluralStringResource(Res.plurals.note_count, state.noteCount, state.noteCount),
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp).rise(1),
            )
        }
        SectionHeader(stringResource(Res.string.settings_appearance))
        SettingsCard(Modifier.rise(2)) {
            Text(stringResource(Res.string.settings_theme), style = MaterialTheme.typography.bodyLarge)
            val modes = ThemeMode.entries
            FilmorySegmentedRow(
                options = modes.map { it.label() },
                selectedIndex = modes.indexOf(state.themeMode),
                onSelect = { onAction(SettingsAction.OnThemeModeChange(modes[it])) },
            )
            SettingSwitch(
                label = stringResource(Res.string.settings_dynamic_color),
                checked = state.useDynamicColor && isDynamicColorAvailable,
                enabled = isDynamicColorAvailable,
                onCheckedChange = { onAction(SettingsAction.OnDynamicColorChange(it)) },
            )
            if (!isDynamicColorAvailable) {
                Text(
                    text = stringResource(Res.string.settings_dynamic_color_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        dataSection()
        SectionHeader(stringResource(Res.string.settings_about))
        SettingsCard(Modifier.rise(4)) {
            Text(
                text = stringResource(Res.string.settings_local_first),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TmdbAttribution()
        }
    }
}

/** A card holding a group of settings. */
@Composable
fun SettingsCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FilmoryCard(modifier.fillMaxWidth().padding(horizontal = ScreenPadding)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.SYSTEM -> Res.string.settings_theme_system
        ThemeMode.LIGHT -> Res.string.settings_theme_light
        ThemeMode.DARK -> Res.string.settings_theme_dark
    },
)

@Preview
@Composable
private fun SettingsScreenPreview() {
    FilmoryTheme { SettingsScreen(SettingsState(titleCount = 11, watchedCount = 6, noteCount = 5), onAction = {}) }
}

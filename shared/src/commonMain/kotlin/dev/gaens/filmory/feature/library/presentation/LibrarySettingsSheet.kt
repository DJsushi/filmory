package dev.gaens.filmory.feature.library.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.SettingSwitch
import dev.gaens.filmory.feature.settings.domain.LibraryLayout
import dev.gaens.filmory.feature.settings.domain.LibrarySettings
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.library_include_wishlist
import dev.gaens.filmory.resources.library_layout_grid
import dev.gaens.filmory.resources.library_layout_list
import dev.gaens.filmory.resources.library_settings_title
import dev.gaens.filmory.resources.library_show_stars
import dev.gaens.filmory.resources.library_show_status_icon
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySettingsSheet(
    settings: LibrarySettings,
    onChange: (LibrarySettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.library_settings_title), style = MaterialTheme.typography.headlineSmall)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                LibraryLayout.entries.forEachIndexed { index, layout ->
                    SegmentedButton(
                        selected = settings.layout == layout,
                        onClick = { onChange(settings.copy(layout = layout)) },
                        shape = SegmentedButtonDefaults.itemShape(index, LibraryLayout.entries.size),
                    ) {
                        Text(
                            stringResource(
                                if (layout == LibraryLayout.GRID) Res.string.library_layout_grid else Res.string.library_layout_list,
                            ),
                        )
                    }
                }
            }
            SettingSwitch(stringResource(Res.string.library_show_stars), settings.showStars) {
                onChange(settings.copy(showStars = it))
            }
            SettingSwitch(stringResource(Res.string.library_show_status_icon), settings.showStatusIcon) {
                onChange(settings.copy(showStatusIcon = it))
            }
            SettingSwitch(stringResource(Res.string.library_include_wishlist), settings.includeWishlist) {
                onChange(settings.copy(includeWishlist = it))
            }
        }
    }
}

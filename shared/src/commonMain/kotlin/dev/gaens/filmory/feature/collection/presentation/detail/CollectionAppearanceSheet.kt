package dev.gaens.filmory.feature.collection.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.FilmorySegmentedRow
import dev.gaens.filmory.core.presentation.designsystem.components.posterGradient
import dev.gaens.filmory.core.presentation.designsystem.readableContentOn
import dev.gaens.filmory.core.presentation.ui.rememberImagePicker
import dev.gaens.filmory.feature.collection.domain.CollectionIcon
import dev.gaens.filmory.feature.collection.domain.collectionIconOf
import dev.gaens.filmory.feature.collection.presentation.CollectionCardSurface
import dev.gaens.filmory.feature.collection.presentation.CollectionColorPresets
import dev.gaens.filmory.feature.collection.presentation.CollectionIconView
import dev.gaens.filmory.feature.collection.presentation.CollectionLook
import dev.gaens.filmory.feature.collection.presentation.CollectionSymbols
import dev.gaens.filmory.feature.collection.presentation.Hsv
import dev.gaens.filmory.feature.collection.presentation.colorArgbOf
import dev.gaens.filmory.feature.collection.presentation.hsvOf
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_done
import dev.gaens.filmory.resources.collection_appearance_brightness
import dev.gaens.filmory.resources.collection_appearance_choose_photo
import dev.gaens.filmory.resources.collection_appearance_color
import dev.gaens.filmory.resources.collection_appearance_color_custom
import dev.gaens.filmory.resources.collection_appearance_color_default
import dev.gaens.filmory.resources.collection_appearance_emoji_hint
import dev.gaens.filmory.resources.collection_appearance_hue
import dev.gaens.filmory.resources.collection_appearance_icon
import dev.gaens.filmory.resources.collection_appearance_icon_emoji
import dev.gaens.filmory.resources.collection_appearance_icon_none
import dev.gaens.filmory.resources.collection_appearance_icon_photo
import dev.gaens.filmory.resources.collection_appearance_icon_symbols
import dev.gaens.filmory.resources.collection_appearance_remove_photo
import dev.gaens.filmory.resources.collection_appearance_saturation
import dev.gaens.filmory.resources.collection_appearance_title
import dev.gaens.filmory.resources.ic_check
import dev.gaens.filmory.resources.ic_close
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val QuickEmoji = listOf("🎬", "🍿", "🎞️", "📺", "🌧️", "🌙", "😱", "😂", "❤️", "⭐", "🔥", "👻", "🚀", "🎭", "🧠", "💤")
private const val MAX_EMOJI_LENGTH = 12

private enum class IconTab { SYMBOLS, EMOJI, PHOTO }

/**
 * Sheet to choose a collection's icon (a drawn symbol, an emoji or a photo) and card color (a preset
 * or any color). Every choice is saved at once and shows in the preview at the top.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CollectionAppearanceSheet(
    collectionId: String,
    name: String,
    look: CollectionLook,
    onAction: (CollectionDetailAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(CollectionDetailAction.OnAppearanceDismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        val icon = collectionIconOf(look.iconKey)
        var tab by remember {
            mutableStateOf(
                when (icon) {
                    is CollectionIcon.Emoji -> IconTab.EMOJI
                    is CollectionIcon.Photo -> IconTab.PHOTO
                    else -> IconTab.SYMBOLS
                },
            )
        }
        // The color being dragged shows at once; it is saved when the finger lifts.
        var draftColor by remember { mutableStateOf<Long?>(null) }
        val shownLook = look.copy(colorArgb = draftColor ?: look.colorArgb)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.collection_appearance_title), style = MaterialTheme.typography.headlineSmall)
            CollectionCardSurface(
                look = shownLook,
                seed = collectionId,
                modifier = Modifier.fillMaxWidth().height(96.dp),
                onClick = {},
            ) { contentColor ->
                Text(name, style = MaterialTheme.typography.titleMedium, color = contentColor, maxLines = 1)
            }

            Text(stringResource(Res.string.collection_appearance_icon), style = MaterialTheme.typography.labelLarge)
            val tabs = IconTab.entries
            FilmorySegmentedRow(
                options = listOf(
                    stringResource(Res.string.collection_appearance_icon_symbols),
                    stringResource(Res.string.collection_appearance_icon_emoji),
                    stringResource(Res.string.collection_appearance_icon_photo),
                ),
                selectedIndex = tabs.indexOf(tab),
                onSelect = { tab = tabs[it] },
            )
            when (tab) {
                IconTab.SYMBOLS -> SymbolGrid(icon, onAction)
                IconTab.EMOJI -> EmojiPicker(icon, onAction)
                IconTab.PHOTO -> PhotoPicker(icon, shownLook, onAction)
            }

            Text(stringResource(Res.string.collection_appearance_color), style = MaterialTheme.typography.labelLarge)
            ColorPicker(
                colorArgb = look.colorArgb,
                collectionId = collectionId,
                onPreview = { draftColor = it },
                onCommit = {
                    draftColor = null
                    onAction(CollectionDetailAction.OnColorChange(it))
                },
            )
            Button(onClick = { onAction(CollectionDetailAction.OnAppearanceDismiss) }, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(Res.string.action_done))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymbolGrid(selected: CollectionIcon?, onAction: (CollectionDetailAction) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Choice(
            isSelected = selected == null,
            description = stringResource(Res.string.collection_appearance_icon_none),
            onClick = { onAction(CollectionDetailAction.OnIconChange(null)) },
        ) {
            Icon(painterResource(Res.drawable.ic_close), null, Modifier.size(24.dp), MaterialTheme.colorScheme.onSurfaceVariant)
        }
        CollectionSymbols.forEach { (name, drawable) ->
            Choice(
                isSelected = selected == CollectionIcon.Symbol(name),
                description = name,
                onClick = { onAction(CollectionDetailAction.OnIconChange(CollectionIcon.Symbol(name))) },
            ) {
                Icon(painterResource(drawable), null, Modifier.size(24.dp), MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiPicker(selected: CollectionIcon?, onAction: (CollectionDetailAction) -> Unit) {
    var typed by remember { mutableStateOf((selected as? CollectionIcon.Emoji)?.text.orEmpty()) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickEmoji.forEach { emoji ->
            Choice(
                isSelected = selected == CollectionIcon.Emoji(emoji),
                description = emoji,
                onClick = {
                    typed = emoji
                    onAction(CollectionDetailAction.OnIconChange(CollectionIcon.Emoji(emoji)))
                },
            ) { Text(emoji, style = MaterialTheme.typography.titleLarge, maxLines = 1) }
        }
    }
    OutlinedTextField(
        value = typed,
        onValueChange = { value ->
            typed = value.take(MAX_EMOJI_LENGTH)
            if (typed.isNotBlank()) onAction(CollectionDetailAction.OnIconChange(CollectionIcon.Emoji(typed.trim())))
        },
        label = { Text(stringResource(Res.string.collection_appearance_emoji_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PhotoPicker(selected: CollectionIcon?, look: CollectionLook, onAction: (CollectionDetailAction) -> Unit) {
    val pickImage = rememberImagePicker { onAction(CollectionDetailAction.OnPhotoPicked(it)) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (selected is CollectionIcon.Photo) CollectionIconView(look, Color.Unspecified, 56.dp)
        FilledTonalButton(onClick = pickImage) { Text(stringResource(Res.string.collection_appearance_choose_photo)) }
        if (selected is CollectionIcon.Photo) {
            TextButton(onClick = { onAction(CollectionDetailAction.OnIconChange(null)) }) {
                Text(stringResource(Res.string.collection_appearance_remove_photo))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(colorArgb: Long?, collectionId: String, onPreview: (Long) -> Unit, onCommit: (Long?) -> Unit) {
    val isPreset = colorArgb == null || colorArgb in CollectionColorPresets
    var isCustomOpen by remember { mutableStateOf(!isPreset) }
    var hsv by remember { mutableStateOf(colorArgb?.let(::hsvOf) ?: Hsv(210f, 0.6f, 0.7f)) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Swatch(
            isSelected = colorArgb == null,
            description = stringResource(Res.string.collection_appearance_color_default),
            background = posterGradient(collectionId),
            checkColor = Color.White,
            onClick = {
                isCustomOpen = false
                onCommit(null)
            },
        )
        CollectionColorPresets.forEach { argb ->
            Swatch(
                isSelected = colorArgb == argb,
                description = "#${(argb and 0xFFFFFF).toString(16).padStart(6, '0')}",
                background = Brush.linearGradient(listOf(Color(argb.toInt()), Color(argb.toInt()))),
                checkColor = readableContentOn(Color(argb.toInt())),
                onClick = {
                    isCustomOpen = false
                    onCommit(argb)
                },
            )
        }
        Swatch(
            isSelected = isCustomOpen,
            description = stringResource(Res.string.collection_appearance_color_custom),
            background = Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)),
            checkColor = Color.White,
            onClick = {
                isCustomOpen = true
                onCommit(colorArgbOf(hsv))
            },
        )
    }
    if (isCustomOpen) {
        val hueTrack = remember { Brush.horizontalGradient((0..6).map { Color.hsv(it * 60f, 1f, 1f) }) }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val commit = { onCommit(colorArgbOf(hsv)) }
            ColorSlider(
                label = stringResource(Res.string.collection_appearance_hue),
                value = hsv.hue / 360f,
                track = hueTrack,
                onChange = {
                    hsv = hsv.copy(hue = it * 360f)
                    onPreview(colorArgbOf(hsv))
                },
                onFinished = commit,
            )
            ColorSlider(
                label = stringResource(Res.string.collection_appearance_saturation),
                value = hsv.saturation,
                track = null,
                onChange = {
                    hsv = hsv.copy(saturation = it)
                    onPreview(colorArgbOf(hsv))
                },
                onFinished = commit,
            )
            ColorSlider(
                label = stringResource(Res.string.collection_appearance_brightness),
                value = hsv.value,
                track = null,
                onChange = {
                    hsv = hsv.copy(value = it)
                    onPreview(colorArgbOf(hsv))
                },
                onFinished = commit,
            )
        }
    }
}

@Composable
private fun ColorSlider(label: String, value: Float, track: Brush?, onChange: (Float) -> Unit, onFinished: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(96.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (track != null) Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(track))
            Slider(
                value = value,
                onValueChange = onChange,
                onValueChangeFinished = onFinished,
                // The hue slider shows its rainbow instead of the usual track.
                colors = if (track == null) SliderDefaults.colors() else SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun Swatch(isSelected: Boolean, description: String, background: Brush, checkColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
            .semantics {
                contentDescription = description
                selected = isSelected
            }
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) Icon(painterResource(Res.drawable.ic_check), null, Modifier.size(20.dp), checkColor)
    }
}

@Composable
private fun Choice(isSelected: Boolean, description: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
            .semantics {
                contentDescription = description
                selected = isSelected
            }
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

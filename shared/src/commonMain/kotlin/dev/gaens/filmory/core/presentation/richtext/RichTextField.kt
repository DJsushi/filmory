package dev.gaens.filmory.core.presentation.richtext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.gaens.filmory.core.presentation.designsystem.LocalReducedMotion
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_done
import dev.gaens.filmory.resources.ic_expand
import dev.gaens.filmory.resources.richtext_bold
import dev.gaens.filmory.resources.richtext_bullet
import dev.gaens.filmory.resources.richtext_expand
import dev.gaens.filmory.resources.richtext_heading
import dev.gaens.filmory.resources.richtext_italic
import dev.gaens.filmory.resources.richtext_quote
import dev.gaens.filmory.resources.richtext_strikethrough
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Keeps a [TextFieldValue] (text and selection) in step with a plain string that lives elsewhere. */
@Composable
private fun rememberFieldState(value: String): androidx.compose.runtime.MutableState<TextFieldValue> {
    val state = remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    if (state.value.text != value) state.value = TextFieldValue(value, TextRange(value.length))
    return state
}

/**
 * A text field for longer text written in Markdown, shown styled as you type. A button in its
 * corner opens the full-screen editor with the formatting toolbar.
 */
@Composable
fun RichTextField(
    value: String,
    onValueChange: (String) -> Unit,
    editorTitle: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    minLines: Int = 3,
    maxLines: Int = Int.MAX_VALUE,
    shape: Shape = OutlinedTextFieldDefaults.shape,
) {
    var field by rememberFieldState(value)
    var isExpanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val colors = rememberMarkdownColors()
    val reveal = if (isFocused) field.selection else null
    OutlinedTextField(
        value = field,
        onValueChange = {
            field = it
            onValueChange(it.text)
        },
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        shape = shape,
        visualTransformation = remember(colors, reveal) { MarkdownVisualTransformation(colors, reveal) },
        minLines = minLines,
        maxLines = maxLines,
        trailingIcon = {
            IconButton(onClick = { isExpanded = true }) {
                Icon(painterResource(Res.drawable.ic_expand), stringResource(Res.string.richtext_expand))
            }
        },
        modifier = modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
    )
    if (isExpanded) {
        RichTextEditor(editorTitle, value, onValueChange, onDismiss = { isExpanded = false })
    }
}

/** The full-screen editor: a big styled text area and, above the keyboard, the formatting buttons. */
@Composable
fun RichTextEditor(title: String, value: String, onValueChange: (String) -> Unit, onDismiss: () -> Unit) {
    var field by rememberFieldState(value)
    val colors = rememberMarkdownColors()
    val focusRequester = remember { FocusRequester() }
    var isShown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isShown = true
        focusRequester.requestFocus()
    }
    val isReduced = LocalReducedMotion.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AnimatedVisibility(
            visible = isShown,
            enter = if (isReduced) EnterTransition.None else fadeIn() + scaleIn(initialScale = 0.92f),
            exit = ExitTransition.None,
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = ScreenPadding, end = 8.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_done)) }
                    }
                    BasicTextField(
                        value = field,
                        onValueChange = {
                            field = it
                            onValueChange(it.text)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = remember(colors, field.selection) { MarkdownVisualTransformation(colors, field.selection) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = ScreenPadding, vertical = 8.dp)
                            .focusRequester(focusRequester),
                    )
                    FormattingToolbar { edit ->
                        val selection = field.selection
                        val result = edit(TextEdit(field.text, selection.min, selection.max))
                        field = TextFieldValue(result.text, TextRange(result.start, result.end))
                        onValueChange(result.text)
                    }
                }
            }
        }
    }
}

private class FormatButton(val label: androidx.compose.ui.text.AnnotatedString, val description: String, val edit: (TextEdit) -> TextEdit)

@Composable
private fun FormattingToolbar(apply: ((TextEdit) -> TextEdit) -> Unit) {
    fun styled(text: String, style: SpanStyle) = buildAnnotatedString { withStyle(style) { append(text) } }
    val buttons = listOf(
        FormatButton(styled("B", SpanStyle(fontWeight = FontWeight.Bold)), stringResource(Res.string.richtext_bold)) { toggleWrap(it, "**") },
        FormatButton(styled("I", SpanStyle(fontStyle = FontStyle.Italic)), stringResource(Res.string.richtext_italic)) { toggleWrap(it, "*") },
        FormatButton(styled("S", SpanStyle(textDecoration = TextDecoration.LineThrough)), stringResource(Res.string.richtext_strikethrough)) { toggleWrap(it, "~~") },
        FormatButton(styled("H", SpanStyle(fontWeight = FontWeight.Bold)), stringResource(Res.string.richtext_heading)) { toggleBlock(it, BlockKind.HEADING) },
        FormatButton(styled("❝", SpanStyle()), stringResource(Res.string.richtext_quote)) { toggleBlock(it, BlockKind.QUOTE) },
        FormatButton(styled("•", SpanStyle(fontWeight = FontWeight.Bold)), stringResource(Res.string.richtext_bullet)) { toggleBlock(it, BlockKind.BULLET) },
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        buttons.forEach { button ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .semantics { contentDescription = button.description }
                    .clickable(role = Role.Button) { apply(button.edit) },
                contentAlignment = Alignment.Center,
            ) {
                Text(button.label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

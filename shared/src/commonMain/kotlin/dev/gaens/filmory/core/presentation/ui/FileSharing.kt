package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable

/** Returns a function that hands a file to the system share sheet. */
@Composable
expect fun rememberFileSharer(): suspend (path: String, mimeType: String) -> Unit

/**
 * Returns a function that opens the system "save as" screen, suggests the name of the file at
 * `path`, and copies that file to the place the user picks. [onResult] gets false when the copy
 * failed and true when it worked; backing out of the screen reports nothing.
 */
@Composable
expect fun rememberFileSaver(onResult: (isSaved: Boolean) -> Unit): (path: String) -> Unit

/** Returns a function that opens the system file picker and reports a private copy of the picked file. */
@Composable
expect fun rememberFilePicker(onFilePicked: (path: String) -> Unit): () -> Unit

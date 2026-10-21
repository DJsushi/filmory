package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable

// shortcut: no share sheet or file picker on iOS yet; use UIActivityViewController and UIDocumentPickerViewController
// when the iOS app is built.
@Composable
actual fun rememberFileSharer(): suspend (path: String, mimeType: String) -> Unit = { _, _ -> }

// shortcut: no save screen on iOS yet; use UIDocumentPickerViewController(forExporting:) when the iOS app is built.
@Composable
actual fun rememberFileSaver(onResult: (isSaved: Boolean) -> Unit): (path: String) -> Unit = {}

@Composable
actual fun rememberFilePicker(onFilePicked: (path: String) -> Unit): () -> Unit = {}

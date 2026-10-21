package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable

// shortcut: no photo picker on iOS yet; wire PHPickerViewController when the iOS app is built.
@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): () -> Unit = {}

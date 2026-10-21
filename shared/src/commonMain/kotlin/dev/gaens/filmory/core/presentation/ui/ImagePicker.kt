package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable

/** Returns a function that opens the system photo picker and reports the picked image's bytes. */
@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): () -> Unit

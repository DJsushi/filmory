package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

// shortcut: no share sheet on iOS yet; present a UIActivityViewController when the iOS app is built.
@Composable
actual fun rememberImageSharer(): suspend (image: ImageBitmap, fileName: String) -> Unit = { _, _ -> }

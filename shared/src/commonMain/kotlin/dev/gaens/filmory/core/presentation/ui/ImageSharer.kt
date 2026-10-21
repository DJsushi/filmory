package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Returns a function that hands an image to the system share sheet as a PNG file. */
@Composable
expect fun rememberImageSharer(): suspend (image: ImageBitmap, fileName: String) -> Unit

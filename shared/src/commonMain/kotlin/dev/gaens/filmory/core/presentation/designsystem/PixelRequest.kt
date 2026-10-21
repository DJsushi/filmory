package dev.gaens.filmory.core.presentation.designsystem

import coil3.request.ImageRequest

/** Asks for a bitmap that can be drawn into an [androidx.compose.ui.graphics.ImageBitmap] and read back. */
internal expect fun ImageRequest.Builder.forPixelReading(): ImageRequest.Builder

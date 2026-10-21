package dev.gaens.filmory.core.presentation.designsystem

import coil3.request.ImageRequest
import coil3.request.allowHardware

// Hardware bitmaps cannot be drawn on a software canvas.
internal actual fun ImageRequest.Builder.forPixelReading(): ImageRequest.Builder = allowHardware(false)

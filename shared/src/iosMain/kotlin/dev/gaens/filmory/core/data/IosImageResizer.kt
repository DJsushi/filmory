package dev.gaens.filmory.core.data

import dev.gaens.filmory.core.domain.ImageResizer

// shortcut: returns the picked bytes as they are, so a custom collection icon is stored at full size;
// scale it with UIGraphicsImageRenderer when the iOS app is built.
class IosImageResizer : ImageResizer {
    override suspend fun centerCropSquare(bytes: ByteArray, sizePx: Int): ByteArray = bytes
}

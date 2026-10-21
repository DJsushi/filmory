package dev.gaens.filmory.core.domain

/** Platform image work that has no common-code equivalent. */
interface ImageResizer {
    /**
     * Crops [bytes], an encoded image, to the square in its middle and scales it to [sizePx] pixels a
     * side. Returns a JPEG, or null when the bytes are not an image the platform can read.
     */
    suspend fun centerCropSquare(bytes: ByteArray, sizePx: Int): ByteArray?
}

package dev.gaens.filmory.core.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import dev.gaens.filmory.core.domain.ImageResizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.min

private const val JPEG_QUALITY = 88

class AndroidImageResizer : ImageResizer {

    override suspend fun centerCropSquare(bytes: ByteArray, sizePx: Int): ByteArray? = withContext(Dispatchers.Default) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        // Decode at a power-of-two size close to the target, so a large photo does not fill memory.
        var sampleSize = 1
        while (min(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= sizePx) sampleSize *= 2
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sampleSize })
            ?: return@withContext null

        val upright = decoded.rotatedByExif(bytes)
        val side = min(upright.width, upright.height)
        val cropped = Bitmap.createBitmap(upright, (upright.width - side) / 2, (upright.height - side) / 2, side, side)
        val scaled = Bitmap.createScaledBitmap(cropped, sizePx, sizePx, true)
        ByteArrayOutputStream().also { scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }.toByteArray()
    }

    private fun Bitmap.rotatedByExif(bytes: ByteArray): Bitmap {
        val degrees = try {
            when (ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: IOException) {
            0f
        }
        if (degrees == 0f) return this
        return Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postRotate(degrees) }, true)
    }
}

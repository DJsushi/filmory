package dev.gaens.filmory.core.presentation.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val EXPORTS_DIRECTORY = "exports"
private const val PNG_QUALITY = 100

@Composable
actual fun rememberImageSharer(): suspend (image: ImageBitmap, fileName: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { image, fileName ->
            val file = withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, EXPORTS_DIRECTORY).apply { mkdirs() }
                File(directory, fileName).apply {
                    outputStream().use { image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
                }
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", file)
            val send = Intent(Intent.ACTION_SEND)
                .setType("image/png")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

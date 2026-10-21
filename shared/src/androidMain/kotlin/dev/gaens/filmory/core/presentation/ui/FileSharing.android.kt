package dev.gaens.filmory.core.presentation.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val IMPORTS_DIRECTORY = "imports"

@Composable
actual fun rememberFileSharer(): suspend (path: String, mimeType: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { path, mimeType ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", File(path))
            val send = Intent(Intent.ACTION_SEND)
                .setType(mimeType)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

@Composable
actual fun rememberFilePicker(onFilePicked: (path: String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val copy = withContext(Dispatchers.IO) {
                val target = File(File(context.cacheDir, IMPORTS_DIRECTORY).apply { mkdirs() }, "import.tmp")
                val opened = context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { input.copyTo(it) }
                }
                target.takeIf { opened != null }
            }
            if (copy != null) onFilePicked(copy.absolutePath)
        }
    }
    return { launcher.launch(arrayOf("*/*")) }
}

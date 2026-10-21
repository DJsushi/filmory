package dev.gaens.filmory.core.data

import okio.Path

/** App-private directories, supplied by each platform. [files] survives app updates and is backed up; [cache] holds temporary files such as exports. */
data class AppDirectories(val files: Path, val cache: Path)

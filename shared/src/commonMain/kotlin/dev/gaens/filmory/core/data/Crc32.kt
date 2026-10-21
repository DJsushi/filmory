package dev.gaens.filmory.core.data

private val CrcTable = IntArray(256) { index ->
    var crc = index
    repeat(8) { crc = if (crc and 1 != 0) (crc ushr 1) xor 0xEDB88320.toInt() else crc ushr 1 }
    crc
}

/** CRC-32 as used by zip files. */
internal fun crc32(bytes: ByteArray): Int {
    var crc = -1
    for (byte in bytes) crc = CrcTable[(crc xor byte.toInt()) and 0xFF] xor (crc ushr 8)
    return crc.inv()
}

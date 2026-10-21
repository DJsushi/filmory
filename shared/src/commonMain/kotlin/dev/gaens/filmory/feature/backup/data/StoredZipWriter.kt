package dev.gaens.filmory.feature.backup.data

import dev.gaens.filmory.core.data.crc32
import okio.BufferedSink

private const val LOCAL_HEADER_SIGNATURE = 0x04034b50
private const val CENTRAL_HEADER_SIGNATURE = 0x02014b50
private const val END_SIGNATURE = 0x06054b50
private const val ZIP_VERSION = 20
private const val DOS_DATE_1980_01_01 = 33

/**
 * Writes a zip without compression ("stored"). Okio reads zips but does not write them, and a stored
 * archive is a valid zip that every tool opens. Entries are held in memory.
 * shortcut: no zip64, so the archive stays under 4 GB and 65,535 entries; fine for a personal library.
 */
internal class StoredZipWriter(private val sink: BufferedSink) {
    private class Entry(val name: ByteArray, val crc: Int, val size: Int, val offset: Int)

    private val entries = mutableListOf<Entry>()
    private var position = 0

    fun add(name: String, data: ByteArray) {
        val nameBytes = name.encodeToByteArray()
        val crc = crc32(data)
        entries += Entry(nameBytes, crc, data.size, position)
        sink.writeIntLe(LOCAL_HEADER_SIGNATURE)
        sink.writeShortLe(ZIP_VERSION)
        sink.writeShortLe(0) // flags
        sink.writeShortLe(0) // method: stored
        sink.writeShortLe(0) // time
        sink.writeShortLe(DOS_DATE_1980_01_01)
        sink.writeIntLe(crc)
        sink.writeIntLe(data.size)
        sink.writeIntLe(data.size)
        sink.writeShortLe(nameBytes.size)
        sink.writeShortLe(0) // extra length
        sink.write(nameBytes)
        sink.write(data)
        position += LOCAL_HEADER_FIXED_SIZE + nameBytes.size + data.size
    }

    fun finish() {
        val directoryStart = position
        var directorySize = 0
        entries.forEach { entry ->
            sink.writeIntLe(CENTRAL_HEADER_SIGNATURE)
            sink.writeShortLe(ZIP_VERSION) // made by
            sink.writeShortLe(ZIP_VERSION) // needed
            sink.writeShortLe(0)
            sink.writeShortLe(0)
            sink.writeShortLe(0)
            sink.writeShortLe(DOS_DATE_1980_01_01)
            sink.writeIntLe(entry.crc)
            sink.writeIntLe(entry.size)
            sink.writeIntLe(entry.size)
            sink.writeShortLe(entry.name.size)
            sink.writeShortLe(0) // extra
            sink.writeShortLe(0) // comment
            sink.writeShortLe(0) // disk
            sink.writeShortLe(0) // internal attributes
            sink.writeIntLe(0) // external attributes
            sink.writeIntLe(entry.offset)
            sink.write(entry.name)
            directorySize += CENTRAL_HEADER_FIXED_SIZE + entry.name.size
        }
        sink.writeIntLe(END_SIGNATURE)
        sink.writeShortLe(0)
        sink.writeShortLe(0)
        sink.writeShortLe(entries.size)
        sink.writeShortLe(entries.size)
        sink.writeIntLe(directorySize)
        sink.writeIntLe(directoryStart)
        sink.writeShortLe(0)
        sink.flush()
    }

    private companion object {
        const val LOCAL_HEADER_FIXED_SIZE = 30
        const val CENTRAL_HEADER_FIXED_SIZE = 46
    }
}

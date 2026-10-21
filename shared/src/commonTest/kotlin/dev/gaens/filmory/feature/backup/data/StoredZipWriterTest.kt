package dev.gaens.filmory.feature.backup.data

import dev.gaens.filmory.core.data.crc32
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import okio.openZip
import kotlin.test.Test
import kotlin.test.assertEquals

class StoredZipWriterTest {

    @Test
    fun `crc32 matches the standard check value`() {
        assertEquals(0xCBF43926.toInt(), crc32("123456789".encodeToByteArray()))
    }

    @Test
    fun `an archive written here can be read back as a zip`() {
        val fileSystem = FakeFileSystem()
        val file = "/backup.zip".toPath()
        fileSystem.write(file) {
            StoredZipWriter(this).apply {
                add("data.json", """{"a":1}""".encodeToByteArray())
                add("note_images/x.jpg", byteArrayOf(1, 2, 3, 4))
                add("empty.txt", ByteArray(0))
                finish()
            }
        }

        val zip = fileSystem.openZip(file)

        assertEquals("""{"a":1}""", zip.read("/data.json".toPath()) { readUtf8() })
        assertEquals(listOf<Byte>(1, 2, 3, 4), zip.read("/note_images/x.jpg".toPath()) { readByteArray() }.toList())
        assertEquals(0L, zip.metadata("/empty.txt".toPath()).size)
    }
}

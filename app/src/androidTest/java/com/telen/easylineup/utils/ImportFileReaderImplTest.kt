/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

/**
 * [JsonExportBaseTests] (app/src/test) covers the pure Gson wire-format mapping in isolation -
 * this covers the glue [ImportFileReaderImpl] itself adds on top (a real
 * `ContentResolver.openInputStream`/file read), which needs an actual [android.content.Context]
 * and so cannot be a plain JVM unit test.
 */
class ImportFileReaderImplTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val reader = ImportFileReaderImpl(context)

    private fun writeTempFile(name: String, content: String): File {
        val file = File(context.cacheDir, name)
        file.writeText(content)
        return file
    }

    @Test
    fun readsARealFileThroughTheContentResolverIntoTheExpectedDomainShape() {
        val file = writeTempFile(
            "valid_import.elu",
            """
                {
                  "teams": [
                    {
                      "id": "team-1",
                      "name": "Riverside Hawks",
                      "image": null,
                      "type": 0,
                      "main": true,
                      "createdAt": 1697000000000,
                      "players": [],
                      "tournaments": []
                    }
                  ]
                }
            """.trimIndent()
        )

        val result = reader.read(Uri.fromFile(file).toString())

        assertEquals("Riverside Hawks", result.teams.single().name)
    }

    @Test
    fun throwsWhenTheFileContentIsNotValidJson() {
        val file = writeTempFile("corrupt_import.elu", "not valid json at all")

        assertThrows(Exception::class.java) {
            reader.read(Uri.fromFile(file).toString())
        }
    }

    @Test
    fun throwsWhenTheUriCannotBeOpened() {
        val missingFile = File(context.cacheDir, "does_not_exist.elu")

        assertThrows(Exception::class.java) {
            reader.read(Uri.fromFile(missingFile).toString())
        }
    }

    @Test
    fun queryDisplayNameFallsBackToTheUriLastPathSegmentForAPlainFileUri() {
        val file = writeTempFile("display_name_test.elu", "{}")

        // A plain file:// Uri isn't backed by a ContentProvider exposing OpenableColumns, so
        // this always exercises queryDisplayName's fallback branch, never the cursor one.
        val displayName = reader.queryDisplayName(Uri.fromFile(file).toString())

        assertEquals("display_name_test.elu", displayName)
    }
}

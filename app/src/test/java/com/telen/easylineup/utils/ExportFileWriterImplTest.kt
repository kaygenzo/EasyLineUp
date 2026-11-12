/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.Manifest
import android.app.Application
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.provider.DocumentsProvider
import androidx.test.core.app.ApplicationProvider
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.TeamExport
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [ExportFileWriterImpl] against a folder served by a small in-memory [DocumentsProvider], the
 * same Storage Access Framework calls a real folder picked in the system picker goes through.
 *
 * Not covered: [com.telen.easylineup.domain.ports.WrittenExportFile.folderName]. Reading it
 * queries the provider through the pre-Android-O query() signature, the only one Robolectric's
 * ContentResolver calls, which DocumentsProvider rejects; on a device it is read normally.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ExportFileWriterImplTest {
    private val context: Application = ApplicationProvider.getApplicationContext()
    private val treeUri = DocumentsContract.buildTreeDocumentUri(AUTHORITY, ROOT_ID)

    @Before
    fun registerTheFolder() {
        FolderProvider.dir = File(context.cacheDir, "picked").apply {
            deleteRecursively()
            mkdirs()
        }
        // What a DocumentsProvider must declare in its manifest, or attachInfo() refuses it.
        val info = ProviderInfo().apply {
            authority = AUTHORITY
            exported = true
            grantUriPermissions = true
            readPermission = Manifest.permission.MANAGE_DOCUMENTS
            writePermission = Manifest.permission.MANAGE_DOCUMENTS
        }
        Robolectric.buildContentProvider(FolderProvider::class.java).create(info)
    }

    @Test
    fun writesTheExportIntoThePickedFolder() {
        val export = ExportBase(
            listOf(
                TeamExport(
                    id = "team-1",
                    name = "Hawks",
                    image = null,
                    type = 0,
                    main = true,
                    players = emptyList(),
                    tournaments = emptyList()
                )
            )
        )

        val written = ExportFileWriterImpl(context).write(export, treeUri.toString(), "a.elu")

        assertEquals("a.elu", written.fileName)
        val file = File(FolderProvider.dir, "a.elu")
        assertTrue(file.exists())
        val team = JSONObject(file.readText()).getJSONArray("teams").getJSONObject(0)
        assertEquals("Hawks", team.getString("name"))
    }

    @Test
    fun aFolderThatCannotBeOpenedThrows() {
        val missing = DocumentsContract.buildTreeDocumentUri("com.example.missing", ROOT_ID)

        val error = runCatching {
            val writer = ExportFileWriterImpl(context)
            writer.write(ExportBase(emptyList()), missing.toString(), "a.elu")
        }.exceptionOrNull()

        assertTrue(error != null)
    }

    /** One folder, [dir], holding plain files. Document ids are file names; the root's is ROOT. */
    class FolderProvider : DocumentsProvider() {
        override fun onCreate() = true

        override fun queryRoots(projection: Array<out String>?): Cursor = MatrixCursor(arrayOf())

        override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor {
            val isRoot = documentId == ROOT_ID
            val columns = mapOf(
                Document.COLUMN_DOCUMENT_ID to documentId,
                Document.COLUMN_DISPLAY_NAME to if (isRoot) "Picked folder" else documentId,
                Document.COLUMN_MIME_TYPE to if (isRoot) Document.MIME_TYPE_DIR else FILE_TYPE,
                Document.COLUMN_FLAGS to if (isRoot) Document.FLAG_DIR_SUPPORTS_CREATE else 0
            )
            val names = projection ?: columns.keys.toTypedArray()
            return MatrixCursor(names).apply { addRow(names.map { columns[it] }) }
        }

        override fun queryChildDocuments(
            parentDocumentId: String,
            projection: Array<out String>?,
            sortOrder: String?
        ): Cursor = MatrixCursor(arrayOf(Document.COLUMN_DOCUMENT_ID))

        override fun createDocument(
            parentDocumentId: String,
            mimeType: String,
            displayName: String
        ): String {
            File(dir, displayName).createNewFile()
            return displayName
        }

        override fun openDocument(
            documentId: String,
            mode: String,
            signal: CancellationSignal?
        ): ParcelFileDescriptor = ParcelFileDescriptor.open(
            File(dir, documentId),
            ParcelFileDescriptor.parseMode(mode)
        )

        override fun isChildDocument(parentDocumentId: String, documentId: String) = true

        companion object {
            lateinit var dir: File
        }
    }

    private companion object {
        const val AUTHORITY = "com.telen.easylineup.test.documents"
        const val ROOT_ID = "ROOT"
        const val FILE_TYPE = "application/octet-stream"
    }
}

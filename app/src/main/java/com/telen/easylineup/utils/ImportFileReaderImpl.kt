/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.ImportFileReader
import com.telen.easylineup.export.json.parseExportFile

class ImportFileReaderImpl(private val context: Context) : ImportFileReader {
    override fun read(uri: String): ExportBase {
        val stream = context.contentResolver.openInputStream(uri.toUri())
            ?: throw IllegalArgumentException("Cannot open uri")
        return stream.use { s ->
            s.bufferedReader().use { reader -> parseExportFile(reader) }
        }
    }

    override fun queryDisplayName(uri: String): String {
        val parsed = uri.toUri()
        context.contentResolver.query(parsed, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return parsed.lastPathSegment.orEmpty()
    }
}

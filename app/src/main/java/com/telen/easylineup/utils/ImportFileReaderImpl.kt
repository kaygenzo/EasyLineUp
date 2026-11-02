/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.ImportFileReader
import com.telen.easylineup.export.json.JsonExportBase
import com.telen.easylineup.export.json.exportGson
import com.telen.easylineup.export.json.toDomain

class ImportFileReaderImpl(private val context: Context) : ImportFileReader {
    override fun read(uri: String): ExportBase {
        val stream = context.contentResolver.openInputStream(Uri.parse(uri))
            ?: throw IllegalArgumentException("Cannot open uri")
        return stream.use { s ->
            s.bufferedReader().use { reader ->
                exportGson.fromJson(reader, JsonExportBase::class.java).toDomain()
            }
        }
    }

    override fun queryDisplayName(uri: String): String {
        val parsed = Uri.parse(uri)
        context.contentResolver.query(parsed, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return parsed.lastPathSegment.orEmpty()
    }
}

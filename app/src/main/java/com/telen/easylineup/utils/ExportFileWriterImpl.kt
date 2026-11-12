/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.ExportFileWriter
import com.telen.easylineup.domain.ports.WrittenExportFile
import com.telen.easylineup.export.json.writeExportFile

class ExportFileWriterImpl(private val context: Context) : ExportFileWriter {
    override fun write(
        export: ExportBase,
        directoryUri: String,
        fileName: String
    ): WrittenExportFile {
        val dir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
        val file = dir?.createFile("application/octet-stream", fileName)
            ?: throw Exception("Cannot open directory")
        val outputStream = context.contentResolver.openOutputStream(file.uri)
            ?: throw Exception("Cannot write in file")
        outputStream.use { stream ->
            stream.bufferedWriter().use { writer ->
                writeExportFile(export, writer)
                writer.flush()
            }
        }
        return WrittenExportFile(fileName = fileName, folderName = dir.name)
    }
}

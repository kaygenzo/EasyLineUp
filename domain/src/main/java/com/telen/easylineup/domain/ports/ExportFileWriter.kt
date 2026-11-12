/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports

import com.telen.easylineup.domain.model.export.ExportBase

/**
 * @property fileName the file actually written
 * @property folderName the picked folder's display name, when the provider exposes one
 */
data class WrittenExportFile(val fileName: String, val folderName: String?)

/**
 * Serializes an export and writes it to a user-picked directory, behind the platform's own
 * storage APIs (Android's Storage Access Framework) so domain stays platform-agnostic.
 */
interface ExportFileWriter {
    fun write(export: ExportBase, directoryUri: String, fileName: String): WrittenExportFile
}

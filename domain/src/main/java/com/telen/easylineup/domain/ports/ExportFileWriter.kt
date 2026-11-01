/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports

/**
 * @property fileName the file actually written
 * @property folderName the picked folder's display name, when the provider exposes one
 */
data class WrittenExportFile(val fileName: String, val folderName: String?)

/**
 * Writes an already-serialized export to a user-picked directory, behind the platform's own
 * storage APIs (Android's Storage Access Framework) so domain stays platform-agnostic.
 */
interface ExportFileWriter {
    fun write(content: String, directoryUri: String, fileName: String): WrittenExportFile
}

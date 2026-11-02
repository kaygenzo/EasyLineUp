/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports

import com.telen.easylineup.domain.model.export.ExportBase

/**
 * Reads and parses a user-picked import file, behind the platform's own storage APIs (Android's
 * Storage Access Framework / ContentResolver) so domain stays platform-agnostic.
 */
interface ImportFileReader {
    fun read(uri: String): ExportBase
    fun queryDisplayName(uri: String): String
}

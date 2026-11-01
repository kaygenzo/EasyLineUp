/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.graphics.Bitmap
import android.net.Uri

/**
 * Writes a lineup screen's captured tabs to cache storage and hands back shareable content
 * URIs - the file-system/`FileProvider` side of "share this lineup," kept out of
 * `LineupViewModel` so the ViewModel stays free of direct file I/O.
 */
interface LineupShareImageWriter {
    fun write(views: Map<Int, Bitmap?>): List<Uri>
}

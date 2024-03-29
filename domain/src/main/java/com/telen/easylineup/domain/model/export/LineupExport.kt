/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

import com.google.gson.annotations.SerializedName

/*
Copyright (c) 2020 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */

/**
 * @property id
 * @property name
 * @property eventTime
 * @property createdAt
 * @property editedAt
 * @property mode
 * @property strategy
 * @property extraHitters
 * @property roster
 * @property playerPositions
 * @property playerNumberOverlays
 */
data class LineupExport(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("eventTime") val eventTime: Long,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("editedAt") val editedAt: Long,
    @SerializedName("mode") val mode: Int,
    @SerializedName("strategy") val strategy: Int,
    @SerializedName("extraHitters") var extraHitters: Int,
    @SerializedName("roster") val roster: List<String>?,
    @SerializedName("playerPositions") val playerPositions: List<PlayerPositionExport>,
    @SerializedName("playerNumberOverlays")
    val playerNumberOverlays: List<PlayerNumberOverlayExport>
)

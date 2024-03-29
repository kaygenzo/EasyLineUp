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
 * @property image
 * @property type
 * @property main
 * @property players
 * @property tournaments
 */
data class TeamExport(

    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") var image: String?,
    @SerializedName("type") val type: Int,
    @SerializedName("main") val main: Boolean,
    @SerializedName("players") val players: List<PlayerExport>,
    @SerializedName("tournaments") val tournaments: List<TournamentExport>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as TeamExport

        if (id != other.id) {
            return false
        }
        if (name != other.name) {
            return false
        }
        if (image != other.image) {
            return false
        }
        if (type != other.type) {
            return false
        }
        if (main != other.main) {
            return false
        }
        if (players != other.players) {
            return false
        }
        if (tournaments != other.tournaments) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + type
        result = 31 * result + main.hashCode()
        result = 31 * result + players.hashCode()
        result = 31 * result + tournaments.hashCode()
        return result
    }
}

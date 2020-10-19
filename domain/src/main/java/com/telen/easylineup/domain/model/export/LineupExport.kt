package com.telen.easylineup.domain.model.export

import com.google.gson.annotations.SerializedName

/*
Copyright (c) 2020 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */


data class LineupExport (

		@SerializedName("id") val id : String,
		@SerializedName("name") val name : String,
		@SerializedName("eventTime") val eventTime: Long,
		@SerializedName("createdAt") val createdAt : Long,
		@SerializedName("editedAt") val editedAt : Long,
		@SerializedName("mode") val mode : Int,
		@SerializedName("roster") val roster : List<String>?,
		@SerializedName("playerPositions") val playerPositions : List<PlayerPositionExport>,
		@SerializedName("playerNumberOverlays") val playerNumberOverlays : List<PlayerNumberOverlayExport>
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as LineupExport

		if (id != other.id) return false
		if (name != other.name) return false
		if (eventTime != other.eventTime) return false
		if (createdAt != other.createdAt) return false
		if (editedAt != other.editedAt) return false
		if (mode != other.mode) return false
		if (roster != other.roster) return false
		if (playerPositions != other.playerPositions) return false
		if (playerNumberOverlays != other.playerNumberOverlays) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + eventTime.hashCode()
		result = 31 * result + createdAt.hashCode()
		result = 31 * result + editedAt.hashCode()
		result = 31 * result + mode
		result = 31 * result + (roster?.hashCode() ?: 0)
		result = 31 * result + playerPositions.hashCode()
		result = 31 * result + playerNumberOverlays.hashCode()
		return result
	}
}
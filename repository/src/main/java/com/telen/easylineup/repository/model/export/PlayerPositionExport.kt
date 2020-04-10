package com.telen.easylineup.repository.model.export

import com.google.gson.annotations.SerializedName

/*
Copyright (c) 2020 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */


data class PlayerPositionExport (

		@SerializedName("id") val id : String,
		@SerializedName("playerID") val playerID : String?,
		@SerializedName("position") val position : Int,
		@SerializedName("x") val x : Float,
		@SerializedName("y") val y : Float,
		@SerializedName("flags") val flags: Int,
		@SerializedName("order") val order : Int
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PlayerPositionExport

		if (id != other.id) return false
		if (playerID != other.playerID) return false
		if (position != other.position) return false
		if (x != other.x) return false
		if (y != other.y) return false
		if (flags != other.flags) return false
		if (order != other.order) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + (playerID?.hashCode() ?: 0)
		result = 31 * result + position
		result = 31 * result + x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + order
		result = 31 * result + flags
		return result
	}
}
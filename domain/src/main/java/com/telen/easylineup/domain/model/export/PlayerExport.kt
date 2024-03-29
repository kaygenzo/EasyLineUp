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
 * @property shirtNumber
 * @property licenseNumber
 * @property positions
 * @property pitching
 * @property batting
 * @property email
 * @property phone
 * @property sex
 */
data class PlayerExport(

    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") var image: String?,
    @SerializedName("shirtNumber") val shirtNumber: Int,
    @SerializedName("licenseNumber") val licenseNumber: String,
    @SerializedName("positions") val positions: Int,
    @SerializedName("pitching") val pitching: Int,
    @SerializedName("batting") val batting: Int,
    @SerializedName("email") var email: String?,
    @SerializedName("phone") var phone: String?,
    @SerializedName("sex") var sex: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as PlayerExport

        if (id != other.id) {
            return false
        }
        if (name != other.name) {
            return false
        }
        if (image != other.image) {
            return false
        }
        if (shirtNumber != other.shirtNumber) {
            return false
        }
        if (licenseNumber != other.licenseNumber) {
            return false
        }
        if (positions != other.positions) {
            return false
        }
        if (pitching != other.pitching) {
            return false
        }
        if (batting != other.batting) {
            return false
        }
        if (email != other.email) {
            return false
        }
        if (phone != other.phone) {
            return false
        }
        if (sex != other.sex) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + shirtNumber
        result = 31 * result + licenseNumber.hashCode()
        result = 31 * result + positions
        result = 31 * result + pitching
        result = 31 * result + batting
        result = 31 * result + sex
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        return result
    }
}

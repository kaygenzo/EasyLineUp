/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.PlayerExport
import java.io.Serializable
import java.util.UUID

/**
 * @property id
 * @property teamId
 * @property name
 * @property shirtNumber
 * @property licenseNumber
 * @property image
 * @property positions
 * @property pitching
 * @property batting
 * @property email
 * @property phone
 * @property sex
 * @property hash
 */
data class Player(
    val id: Long = 0,
    val teamId: Long,
    val name: String,
    val shirtNumber: Int,
    val licenseNumber: String,
    val image: String? = null,
    val positions: Int = 0,
    val pitching: Int = 0,
    val batting: Int = 0,
    val email: String? = null,
    val phone: String? = null,
    val sex: Int = 0,
    val hash: String? = UUID.randomUUID().toString()
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Player

        if (id != other.id) {
            return false
        }
        if (teamId != other.teamId) {
            return false
        }
        if (name != other.name) {
            return false
        }
        if (shirtNumber != other.shirtNumber) {
            return false
        }
        if (licenseNumber != other.licenseNumber) {
            return false
        }
        if (image != other.image) {
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
        result = 31 * result + teamId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + shirtNumber
        result = 31 * result + licenseNumber.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + positions
        result = 31 * result + pitching
        result = 31 * result + batting
        result = 31 * result + sex
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        return result
    }
}

fun Player.toPlayerExport(): PlayerExport {
    return PlayerExport(
        hash ?: UUID.randomUUID().toString(),
        name,
        image,
        shirtNumber,
        licenseNumber,
        positions,
        pitching,
        batting,
        email,
        phone,
        sex
    )
}

/**
 * A license number as stored: free text (federations issue alphanumeric ones), trimmed, empty
 * when there is none. A bare "0" is how numeric-only versions of the app wrote "no license",
 * in their database and in their export files, so it is read as none too.
 */
fun normalizeLicenseNumber(raw: String?): String {
    val trimmed = raw?.trim().orEmpty()
    return if (trimmed == LEGACY_NO_LICENSE) "" else trimmed
}

private const val LEGACY_NO_LICENSE = "0"

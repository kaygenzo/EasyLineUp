package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.PlayerExport
import java.util.*

data class Player(
        var id: Long = 0,
        var teamId: Long,
        var name: String,
        var shirtNumber: Int,
        var licenseNumber: Long,
        var image: String? = null,
        var positions: Int = 0,
        var hash: String? = UUID.randomUUID().toString()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (id != other.id) return false
        if (teamId != other.teamId) return false
        if (name != other.name) return false
        if (shirtNumber != other.shirtNumber) return false
        if (licenseNumber != other.licenseNumber) return false
        if (image != other.image) return false
        if (positions != other.positions) return false

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
        return result
    }

}

fun Player.toPlayerExport(): PlayerExport {
    return PlayerExport(hash ?: UUID.randomUUID().toString(), name, image, shirtNumber, licenseNumber.toString(), positions)
}
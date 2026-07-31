/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.annotations.SerializedName
import com.telen.easylineup.domain.model.export.PlayerExport

/**
 * Wire shape of a player entry in an `.elu` file. Field names/keys here are the file format
 * contract - they must not change without a compatibility plan, independently of [PlayerExport]
 * which is free to evolve with the domain's own needs.
 */
internal data class JsonPlayerExport(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,
    @SerializedName("shirtNumber") val shirtNumber: Int,
    @SerializedName("licenseNumber") val licenseNumber: String,
    @SerializedName("positions") val positions: Int,
    @SerializedName("pitching") val pitching: Int,
    @SerializedName("batting") val batting: Int,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("sex") val sex: Int
)

internal fun PlayerExport.toJson(): JsonPlayerExport {
    return JsonPlayerExport(
        id = id,
        name = name,
        image = image,
        shirtNumber = shirtNumber,
        licenseNumber = licenseNumber,
        positions = positions,
        pitching = pitching,
        batting = batting,
        email = email,
        phone = phone,
        sex = sex
    )
}

internal fun JsonPlayerExport.toDomain(): PlayerExport {
    return PlayerExport(
        id = id,
        name = name,
        image = image,
        shirtNumber = shirtNumber,
        licenseNumber = licenseNumber,
        positions = positions,
        pitching = pitching,
        batting = batting,
        email = email,
        phone = phone,
        sex = sex
    )
}

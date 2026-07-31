/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.annotations.SerializedName
import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport

/**
 * Wire shape of a player number overlay entry in an `.elu` file. Field names/keys here are the
 * file format contract - they must not change without a compatibility plan, independently of
 * [PlayerNumberOverlayExport] which is free to evolve with the domain's own needs.
 */
internal data class JsonPlayerNumberOverlayExport(
    @SerializedName("id") val id: String,
    @SerializedName("playerID") val playerId: String?,
    @SerializedName("number") val number: Int
)

internal fun PlayerNumberOverlayExport.toJson(): JsonPlayerNumberOverlayExport {
    return JsonPlayerNumberOverlayExport(
        id = id,
        playerId = playerId,
        number = number
    )
}

internal fun JsonPlayerNumberOverlayExport.toDomain(): PlayerNumberOverlayExport {
    return PlayerNumberOverlayExport(
        id = id,
        playerId = playerId,
        number = number
    )
}

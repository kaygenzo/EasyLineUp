/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.annotations.SerializedName
import com.telen.easylineup.domain.model.export.PlayerPositionExport

/**
 * Wire shape of a player field position entry in an `.elu` file. Field names/keys here are the
 * file format contract - they must not change without a compatibility plan, independently of
 * [PlayerPositionExport] which is free to evolve with the domain's own needs.
 */
internal data class JsonPlayerPositionExport(
    @SerializedName("id") val id: String,
    @SerializedName("playerID") val playerId: String?,
    @SerializedName("position") val position: Int,
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("flags") val flags: Int,
    @SerializedName("order") val order: Int
)

internal fun PlayerPositionExport.toJson(): JsonPlayerPositionExport {
    return JsonPlayerPositionExport(
        id = id,
        playerId = playerId,
        position = position,
        x = x,
        y = y,
        flags = flags,
        order = order
    )
}

internal fun JsonPlayerPositionExport.toDomain(): PlayerPositionExport {
    return PlayerPositionExport(
        id = id,
        playerId = playerId,
        position = position,
        x = x,
        y = y,
        flags = flags,
        order = order
    )
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.annotations.SerializedName
import com.telen.easylineup.domain.model.export.TournamentExport

/**
 * Wire shape of a tournament entry in an `.elu` file. Field names/keys here are the file format
 * contract - they must not change without a compatibility plan, independently of
 * [TournamentExport] which is free to evolve with the domain's own needs.
 */
internal data class JsonTournamentExport(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("startTime") val startTime: Long,
    @SerializedName("endTime") val endTime: Long,
    @SerializedName("address") val address: String?,
    @SerializedName("lineups") val lineups: List<JsonLineupExport>
)

internal fun TournamentExport.toJson(): JsonTournamentExport {
    return JsonTournamentExport(
        id = id,
        name = name,
        createdAt = createdAt,
        startTime = startTime,
        endTime = endTime,
        address = address,
        lineups = lineups.map { it.toJson() }
    )
}

internal fun JsonTournamentExport.toDomain(): TournamentExport {
    return TournamentExport(
        id = id,
        name = name,
        createdAt = createdAt,
        startTime = startTime,
        endTime = endTime,
        address = address,
        lineups = lineups.map { it.toDomain() }
    )
}

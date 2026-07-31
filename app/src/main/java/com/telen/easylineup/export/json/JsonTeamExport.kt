/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.annotations.SerializedName
import com.telen.easylineup.domain.model.export.TeamExport

/**
 * Wire shape of a team entry in an `.elu` file. Field names/keys here are the file format
 * contract - they must not change without a compatibility plan, independently of [TeamExport]
 * which is free to evolve with the domain's own needs.
 */
internal data class JsonTeamExport(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,
    @SerializedName("type") val type: Int,
    @SerializedName("main") val main: Boolean,
    @SerializedName("players") val players: List<JsonPlayerExport>,
    @SerializedName("tournaments") val tournaments: List<JsonTournamentExport>
)

internal fun TeamExport.toJson(): JsonTeamExport {
    return JsonTeamExport(
        id = id,
        name = name,
        image = image,
        type = type,
        main = main,
        players = players.map { it.toJson() },
        tournaments = tournaments.map { it.toJson() }
    )
}

internal fun JsonTeamExport.toDomain(): TeamExport {
    return TeamExport(
        id = id,
        name = name,
        image = image,
        type = type,
        main = main,
        players = players.map { it.toDomain() },
        tournaments = tournaments.map { it.toDomain() }
    )
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.annotations.SerializedName
import com.telen.easylineup.domain.model.export.LineupExport

/**
 * Wire shape of a lineup entry in an `.elu` file. Field names/keys here are the file format
 * contract - they must not change without a compatibility plan, independently of [LineupExport]
 * which is free to evolve with the domain's own needs.
 */
internal data class JsonLineupExport(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("eventTime") val eventTime: Long,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("editedAt") val editedAt: Long,
    @SerializedName("mode") val mode: Int,
    @SerializedName("strategy") val strategy: Int,
    @SerializedName("extraHitters") val extraHitters: Int,
    @SerializedName("roster") val roster: List<String>?,
    @SerializedName("playerPositions") val playerPositions: List<JsonPlayerPositionExport>,
    @SerializedName("playerNumberOverlays")
    val playerNumberOverlays: List<JsonPlayerNumberOverlayExport>
)

internal fun LineupExport.toJson(): JsonLineupExport {
    return JsonLineupExport(
        id = id,
        name = name,
        eventTime = eventTime,
        createdAt = createdAt,
        editedAt = editedAt,
        mode = mode,
        strategy = strategy,
        extraHitters = extraHitters,
        roster = roster,
        playerPositions = playerPositions.map { it.toJson() },
        playerNumberOverlays = playerNumberOverlays.map { it.toJson() }
    )
}

internal fun JsonLineupExport.toDomain(): LineupExport {
    return LineupExport(
        id = id,
        name = name,
        eventTime = eventTime,
        createdAt = createdAt,
        editedAt = editedAt,
        mode = mode,
        strategy = strategy,
        extraHitters = extraHitters,
        roster = roster,
        playerPositions = playerPositions.map { it.toDomain() },
        playerNumberOverlays = playerNumberOverlays.map { it.toDomain() }
    )
}

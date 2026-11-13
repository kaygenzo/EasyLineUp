/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.JsonParseException
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.TeamExport
import java.io.Reader

/**
 * The `.elu` file is not valid JSON, is empty, misses a field the import cannot do without, or
 * points at a player it doesn't contain.
 */
internal class InvalidExportFileException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Reads an `.elu` file into the domain's [ExportBase], or throws [InvalidExportFileException].
 *
 * Gson ignores Kotlin nullability: a field missing from the file, or set to `null`, comes out as
 * `null` even in a non-null `String` or `List`, and only blows up later, far from the read. So
 * the wire types declare every reference field nullable, and the mapping to the domain checks
 * the ones an import needs (ids, which identify rows, and names) and treats a missing list as
 * empty: older files have no `playerNumberOverlays`, for one.
 *
 * A position or number overlay must also point at a player of its own team in the file: the
 * import could only write it with no player, which the database refuses, failing the whole
 * import after the user confirmed it. A roster entry with no such player is just left out.
 */
internal fun parseExportFile(reader: Reader): ExportBase {
    val json = try {
        exportGson.fromJson(reader, JsonExportBase::class.java)
    } catch (e: JsonParseException) {
        throw InvalidExportFileException("Not a valid .elu file", e)
    } ?: throw InvalidExportFileException("Empty .elu file")
    return json.toDomain().also { export -> export.teams.forEach(::checkPlayerReferences) }
}

private fun checkPlayerReferences(team: TeamExport) {
    val playerIds = team.players.mapTo(mutableSetOf()) { it.id }
    team.tournaments.flatMap { it.lineups }.forEach { lineup ->
        val referenced = lineup.playerPositions.map { it.playerId } +
            lineup.playerNumberOverlays.map { it.playerId }
        // A loop, not firstOrNull: the player found missing can be null itself.
        referenced.forEach { playerId ->
            if (playerId !in playerIds) {
                throw InvalidExportFileException(
                    "Lineup ${lineup.id} points at player $playerId, missing from team ${team.id}"
                )
            }
        }
    }
}

internal fun required(value: String?, field: String): String {
    if (value.isNullOrBlank()) {
        throw InvalidExportFileException("Missing $field")
    }
    return value
}

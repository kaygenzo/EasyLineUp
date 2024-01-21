/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport
import com.telen.easylineup.domain.model.export.PlayerPositionExport
import java.util.Calendar
import java.util.UUID

const val MODE_DISABLED = 0
const val MODE_ENABLED = 1

/**
 * @property id
 * @property name
 * @property teamId
 * @property tournamentId
 * @property mode
 * @property strategy
 * @property extraHitters
 * @property eventTimeInMillis
 * @property createdTimeInMillis
 * @property editedTimeInMillis
 * @property roster
 * @property hash
 */
data class Lineup(
    var id: Long = 0,
    var name: String = "",
    var teamId: Long = 0,
    var tournamentId: Long = 0,
    var mode: Int = 0,
    var strategy: Int = TeamStrategy.STANDARD.id,
    var extraHitters: Int = 0,
    var eventTimeInMillis: Long = 0L,
    var createdTimeInMillis: Long = Calendar.getInstance().timeInMillis,
    var editedTimeInMillis: Long = Calendar.getInstance().timeInMillis,
    var roster: String? = null,
    var hash: String? = UUID.randomUUID().toString()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Lineup

        if (id != other.id) {
            return false
        }
        if (name != other.name) {
            return false
        }
        if (teamId != other.teamId) {
            return false
        }
        if (tournamentId != other.tournamentId) {
            return false
        }
        if (mode != other.mode) {
            return false
        }
        if (strategy != other.strategy) {
            return false
        }
        if (extraHitters != other.extraHitters) {
            return false
        }
        if (eventTimeInMillis != other.eventTimeInMillis) {
            return false
        }
        if (roster != other.roster) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + tournamentId.hashCode()
        result = 31 * result + mode
        result = 31 * result + strategy
        result = 31 * result + extraHitters
        result = 31 * result + eventTimeInMillis.hashCode()
        result = 31 * result + createdTimeInMillis.hashCode()
        result = 31 * result + editedTimeInMillis.hashCode()
        result = 31 * result + (roster?.hashCode() ?: 0)
        result = 31 * result + (hash?.hashCode() ?: 0)
        return result
    }
}

fun Lineup.toLineupExport(
    playerPositions: MutableList<PlayerPositionExport>,
    playerNumberOverlays: List<PlayerNumberOverlayExport>,
    rosterUuid: List<String>?
): LineupExport {
    return LineupExport(
        hash ?: UUID.randomUUID().toString(),
        name,
        eventTimeInMillis,
        createdTimeInMillis,
        editedTimeInMillis,
        mode,
        strategy,
        extraHitters,
        rosterUuid,
        playerPositions,
        playerNumberOverlays
    )
}

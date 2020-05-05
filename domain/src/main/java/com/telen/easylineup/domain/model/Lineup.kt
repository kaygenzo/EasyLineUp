package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.PlayerPositionExport
import java.util.*

const val MODE_DISABLED = 0
const val MODE_ENABLED = 1

data class Lineup(
        var id: Long = 0,
        var name: String = "",
        var teamId: Long = 0,
        var tournamentId: Long = 0,
        var mode: Int = 0,
        var createdTimeInMillis: Long = Calendar.getInstance().timeInMillis,
        var editedTimeInMillis: Long = Calendar.getInstance().timeInMillis,
        var roster: String? = null,
        var hash: String? = UUID.randomUUID().toString(),
        val playerPositions: MutableList<FieldPosition> = mutableListOf()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lineup

        if (id != other.id) return false
        if (name != other.name) return false
        if (teamId != other.teamId) return false
        if (tournamentId != other.tournamentId) return false
        if (mode != other.mode) return false
        if (roster != other.roster) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + tournamentId.hashCode()
        result = 31 * result + mode
        result = 31 * result + (roster?.hashCode() ?: 0)
        return result
    }
}

fun Lineup.toLineupExport(playerPositions: MutableList<PlayerPositionExport>, rosterUUID: List<String>?): LineupExport {
    return LineupExport(hash ?: UUID.randomUUID().toString(), name, createdTimeInMillis, editedTimeInMillis, mode, rosterUUID, playerPositions)
}
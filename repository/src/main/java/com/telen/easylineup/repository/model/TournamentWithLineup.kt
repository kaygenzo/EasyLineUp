package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo

data class TournamentWithLineup(
        @ColumnInfo(name = "tournamentID") var tournamentID: Long = 0,
        @ColumnInfo(name = "tournamentName") var tournamentName: String = "",
        @ColumnInfo(name = "tournamentCreatedAt") var tournamentCreatedAt: Long = 0,
        @ColumnInfo(name = "fieldPositionID") var fieldPositionID: Long = 0,
        @ColumnInfo(name = "lineupName") var lineupName: String? = "",
        @ColumnInfo(name = "lineupID") var lineupID: Long = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "position") var position: Int = 0
) {
    fun toTournament() : Tournament {
        return Tournament(id = tournamentID, name = tournamentName, createdAt = tournamentCreatedAt)
    }

    fun toLineup(): Lineup {
        return Lineup(id = lineupID, name = lineupName
                ?: "", tournamentId = tournamentID)
    }
}
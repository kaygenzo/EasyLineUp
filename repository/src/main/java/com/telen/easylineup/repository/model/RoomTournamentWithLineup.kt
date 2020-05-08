package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.TournamentWithLineup

internal data class RoomTournamentWithLineup(
        @ColumnInfo(name = "tournamentID") var tournamentID: Long = 0,
        @ColumnInfo(name = "tournamentName") var tournamentName: String = "",
        @ColumnInfo(name = "tournamentCreatedAt") var tournamentCreatedAt: Long = 0,
        @ColumnInfo(name = "fieldPositionID") var fieldPositionID: Long = 0,
        @ColumnInfo(name = "lineupName") var lineupName: String? = "",
        @ColumnInfo(name = "lineupID") var lineupID: Long = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "teamID") var teamID: Long = 0
)

internal fun RoomTournamentWithLineup.toTournamentWithLineup(): TournamentWithLineup {
    return TournamentWithLineup(tournamentID, tournamentName, tournamentCreatedAt, fieldPositionID, lineupName, lineupID, x, y, position, teamID)
}
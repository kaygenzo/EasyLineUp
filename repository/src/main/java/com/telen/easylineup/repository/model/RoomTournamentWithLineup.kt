/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TournamentWithLineup

/**
 * @property tournamentId
 * @property tournamentName
 * @property tournamentCreatedAt
 * @property tournamentStartTime
 * @property tournamentEndTime
 * @property tournamentAddress
 * @property fieldPositionId
 * @property lineupName
 * @property lineupId
 * @property lineupMode
 * @property lineupStrategy
 * @property lineupExtraHittersSize
 * @property x
 * @property y
 * @property position
 * @property teamId
 * @property lineupEventTime
 * @property lineupCreatedTime
 * @property roster
 */
internal data class RoomTournamentWithLineup(
    @ColumnInfo(name = "tournamentID") var tournamentId: Long = 0,
    @ColumnInfo(name = "tournamentName") var tournamentName: String = "",
    @ColumnInfo(name = "tournamentCreatedAt") var tournamentCreatedAt: Long = 0,
    @ColumnInfo(name = "tournamentStartTime") var tournamentStartTime: Long = 0,
    @ColumnInfo(name = "tournamentEndTime") var tournamentEndTime: Long = 0,
    @ColumnInfo(name = "tournamentAddress") var tournamentAddress: String? = null,
    @ColumnInfo(name = "fieldPositionID") var fieldPositionId: Long = 0,
    @ColumnInfo(name = "lineupName") var lineupName: String? = "",
    @ColumnInfo(name = "lineupID") var lineupId: Long = 0,
    @ColumnInfo(name = "lineupMode") var lineupMode: Int = 0,
    @ColumnInfo(name = "lineupStrategy") var lineupStrategy: Int = TeamStrategy.STANDARD.id,
    @ColumnInfo(name = "lineupExtraHittersSize") var lineupExtraHittersSize: Int = 0,
    @ColumnInfo(name = "x") var x: Float = 0f,
    @ColumnInfo(name = "y") var y: Float = 0f,
    @ColumnInfo(name = "position") var position: Int = 0,
    @ColumnInfo(name = "teamID") var teamId: Long = 0,
    @ColumnInfo(name = "lineupEventTime") var lineupEventTime: Long = 0,
    @ColumnInfo(name = "lineupCreatedTime") var lineupCreatedTime: Long = 0,
    @ColumnInfo(name = "roster") var roster: String? = null
)

internal fun RoomTournamentWithLineup.toTournamentWithLineup(): TournamentWithLineup {
    return TournamentWithLineup(
        tournamentId,
        tournamentName,
        tournamentCreatedAt,
        tournamentStartTime,
        tournamentEndTime,
        tournamentAddress,
        fieldPositionId,
        lineupName,
        lineupId,
        lineupMode,
        lineupStrategy,
        lineupExtraHittersSize,
        x, y,
        position,
        teamId,
        lineupEventTime,
        lineupCreatedTime,
        roster
    )
}

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
    @ColumnInfo(name = "tournamentID") val tournamentId: Long = 0,
    @ColumnInfo(name = "tournamentName") val tournamentName: String = "",
    @ColumnInfo(name = "tournamentCreatedAt") val tournamentCreatedAt: Long = 0,
    @ColumnInfo(name = "tournamentStartTime") val tournamentStartTime: Long = 0,
    @ColumnInfo(name = "tournamentEndTime") val tournamentEndTime: Long = 0,
    @ColumnInfo(name = "tournamentAddress") val tournamentAddress: String? = null,
    @ColumnInfo(name = "fieldPositionID") val fieldPositionId: Long = 0,
    @ColumnInfo(name = "lineupName") val lineupName: String? = "",
    @ColumnInfo(name = "lineupID") val lineupId: Long = 0,
    @ColumnInfo(name = "lineupMode") val lineupMode: Int = 0,
    @ColumnInfo(name = "lineupStrategy") val lineupStrategy: Int = TeamStrategy.STANDARD.id,
    @ColumnInfo(name = "lineupExtraHittersSize") val lineupExtraHittersSize: Int = 0,
    @ColumnInfo(name = "x") val x: Float = 0f,
    @ColumnInfo(name = "y") val y: Float = 0f,
    @ColumnInfo(name = "position") val position: Int = 0,
    @ColumnInfo(name = "teamID") val teamId: Long = 0,
    @ColumnInfo(name = "lineupEventTime") val lineupEventTime: Long = 0,
    @ColumnInfo(name = "lineupCreatedTime") val lineupCreatedTime: Long = 0,
    @ColumnInfo(name = "roster") val roster: String? = null
)

internal fun RoomTournamentWithLineup.toDomain(): TournamentWithLineup {
    return TournamentWithLineup(
        tournamentId = tournamentId,
        tournamentName = tournamentName,
        tournamentCreatedAt = tournamentCreatedAt,
        tournamentStartTime = tournamentStartTime,
        tournamentEndTime = tournamentEndTime,
        tournamentAddress = tournamentAddress,
        fieldPositionId = fieldPositionId,
        lineupName = lineupName,
        lineupId = lineupId,
        lineupMode = lineupMode,
        lineupStrategy = lineupStrategy,
        lineupExtraHittersSize = lineupExtraHittersSize,
        x = x, y = y,
        position = position,
        teamId = teamId,
        lineupEventTime = lineupEventTime,
        lineupCreatedTime = lineupCreatedTime,
        roster = roster
    )
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

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
data class TournamentWithLineup(
    val tournamentId: Long = 0,
    val tournamentName: String = "",
    val tournamentCreatedAt: Long = 0,
    val tournamentStartTime: Long = 0,
    val tournamentEndTime: Long = 0,
    val tournamentAddress: String? = null,
    val fieldPositionId: Long = 0,
    val lineupName: String? = "",
    val lineupId: Long = 0,
    val lineupMode: Int = 0,
    val lineupStrategy: Int = TeamStrategy.STANDARD.id,
    val lineupExtraHittersSize: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val position: Int = 0,
    val teamId: Long = 0,
    val lineupEventTime: Long = 0,
    val lineupCreatedTime: Long = 0,
    val roster: String? = null
)

fun TournamentWithLineup.toTournament(): Tournament {
    return Tournament(
        id = tournamentId,
        name = tournamentName,
        createdAt = tournamentCreatedAt,
        startTime = tournamentStartTime,
        endTime = tournamentEndTime,
        address = tournamentAddress
    )
}

fun TournamentWithLineup.toLineup(): Lineup {
    return Lineup(
        id = lineupId,
        name = lineupName ?: "",
        tournamentId = tournamentId,
        eventTimeInMillis = lineupEventTime,
        teamId = teamId,
        mode = lineupMode,
        createdTimeInMillis = lineupCreatedTime,
        roster = roster,
        strategy = lineupStrategy,
        extraHitters = lineupExtraHittersSize
    )
}

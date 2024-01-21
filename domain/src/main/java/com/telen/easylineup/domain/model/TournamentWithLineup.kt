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
    var tournamentId: Long = 0,
    var tournamentName: String = "",
    var tournamentCreatedAt: Long = 0,
    val tournamentStartTime: Long = 0,
    val tournamentEndTime: Long = 0,
    val tournamentAddress: String? = null,
    var fieldPositionId: Long = 0,
    var lineupName: String? = "",
    var lineupId: Long = 0,
    var lineupMode: Int = 0,
    var lineupStrategy: Int = TeamStrategy.STANDARD.id,
    var lineupExtraHittersSize: Int = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var position: Int = 0,
    var teamId: Long = 0,
    var lineupEventTime: Long = 0,
    var lineupCreatedTime: Long = 0,
    var roster: String? = null
) {
    fun toTournament(): Tournament {
        return Tournament(
            id = tournamentId,
            name = tournamentName,
            createdAt = tournamentCreatedAt,
            startTime = tournamentStartTime,
            endTime = tournamentEndTime,
            address = tournamentAddress
        )
    }

    fun toLineup(): Lineup {
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
}

package com.telen.easylineup.domain.model

data class TournamentWithLineup(
        var tournamentID: Long = 0,
        var tournamentName: String = "",
        var tournamentCreatedAt: Long = 0,
        var fieldPositionID: Long = 0,
        var lineupName: String? = "",
        var lineupID: Long = 0,
        var lineupMode: Int = 0,
        var lineupStrategy: Int = TeamStrategy.STANDARD.id,
        var lineupExtraHittersSize: Int = 0,
        var x: Float = 0f,
        var y: Float = 0f,
        var position: Int = 0,
        var teamID: Long = 0,
        var lineupEventTime: Long = 0,
        var lineupCreatedTime: Long = 0,
        var roster: String? = null
) {
    fun toTournament() : Tournament {
        return Tournament(id = tournamentID, name = tournamentName, createdAt = tournamentCreatedAt)
    }

    fun toLineup(): Lineup {
        return Lineup(id = lineupID, name = lineupName ?: "", tournamentId = tournamentID, eventTimeInMillis = lineupEventTime,
                teamId = teamID, mode = lineupMode, createdTimeInMillis = lineupCreatedTime, roster = roster, strategy = lineupStrategy,
                extraHitters = lineupExtraHittersSize)
    }
}
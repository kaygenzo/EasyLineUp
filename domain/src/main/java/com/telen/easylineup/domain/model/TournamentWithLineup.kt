package com.telen.easylineup.domain.model

data class TournamentWithLineup(
        var tournamentID: Long = 0,
        var tournamentName: String = "",
        var tournamentCreatedAt: Long = 0,
        var fieldPositionID: Long = 0,
        var lineupName: String? = "",
        var lineupID: Long = 0,
        var x: Float = 0f,
        var y: Float = 0f,
        var position: Int = 0,
        var teamID: Long = 0
) {
    fun toTournament() : Tournament {
        return Tournament(id = tournamentID, name = tournamentName, createdAt = tournamentCreatedAt)
    }

    fun toLineup(): Lineup {
        return Lineup(id = lineupID, name = lineupName
                ?: "", tournamentId = tournamentID)
    }
}
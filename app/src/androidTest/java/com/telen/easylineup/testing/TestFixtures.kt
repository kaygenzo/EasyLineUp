/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.getPositionPercentage

/**
 * Plain Kotlin fixture data for instrumented tests - one team, a full STANDARD-strategy
 * roster (9 field positions + DP + 2 bench substitutes), one tournament, and one lineup with
 * a defense diagram + batting order already assigned. Deliberately no player photo URLs
 * ([Player.image] stays null): Coil's real network loading was independently confirmed
 * correct on-device, but a live network round-trip has no place in a deterministic
 * instrumented test.
 */
object TestFixtures {
    const val TEAM_NAME = "Test Team"
    const val SECOND_TEAM_NAME = "Second Team"
    const val TOURNAMENT_NAME = "Test Tournament"
    const val LINEUP_NAME = "Test Lineup"

    private val STRATEGY = TeamStrategy.STANDARD

    fun team(): Team = Team(
        name = TEAM_NAME,
        type = TeamType.BASEBALL.id,
        main = true,
        createdAt = System.currentTimeMillis()
    )

    /** A second, roster-less team - for tests covering switching between teams. */
    fun secondTeam(): Team = Team(
        name = SECOND_TEAM_NAME,
        type = TeamType.BASEBALL.id,
        main = false,
        createdAt = System.currentTimeMillis()
    )

    /** The first team, for a variant other than [team]'s baseball default. */
    fun team(type: TeamType): Team = team().copy(type = type.id)

    /**
     * One player per [strategy] position slot (by default 9 field starters + 1 DP) + 2 bench subs.
     */
    fun players(teamId: Long, strategy: TeamStrategy = STRATEGY): List<Player> {
        val starters = strategy.positions.mapIndexed { index, _ ->
            Player(
                teamId = teamId,
                name = "Starter ${index + 1}",
                shirtNumber = index + 1,
                licenseNumber = 1000L + index
            )
        }
        val substitutes = (1..2).map { index ->
            Player(
                teamId = teamId,
                name = "Substitute $index",
                shirtNumber = 90 + index,
                licenseNumber = 2000L + index
            )
        }
        return starters + substitutes
    }

    fun tournament(): Tournament = Tournament(
        name = TOURNAMENT_NAME,
        createdAt = System.currentTimeMillis(),
        startTime = 0L,
        endTime = 0L
    )

    fun lineup(
        teamId: Long,
        tournamentId: Long,
        strategy: TeamStrategy = STRATEGY
    ): Lineup = Lineup(
        name = LINEUP_NAME,
        teamId = teamId,
        tournamentId = tournamentId,
        strategy = strategy.id,
        extraHitters = 0,
        eventTimeInMillis = System.currentTimeMillis()
    )

    /**
     * [playerIds] must be in the same order [players] returned them - the first
     * [TeamStrategy.positions] entries become field starters, placed at their real canonical
     * diamond coordinate (the same math [getPositionPercentage] uses in production), the rest
     * become bench substitutes.
     */
    fun playerFieldPositions(
        lineupId: Long,
        playerIds: List<Long>,
        strategy: TeamStrategy = STRATEGY
    ): List<PlayerFieldPosition> {
        val starterIds = playerIds.take(strategy.positions.size)
        val substituteIds = playerIds.drop(strategy.positions.size)

        val starterPositions = starterIds.mapIndexed { index, playerId ->
            val position = strategy.positions[index]
            val coordinate = position.getPositionPercentage(strategy)
            PlayerFieldPosition(
                playerId = playerId,
                lineupId = lineupId,
                position = position.id,
                x = coordinate.x,
                y = coordinate.y,
                order = index + 1
            )
        }
        val substitutePositions = substituteIds.mapIndexed { index, playerId ->
            PlayerFieldPosition(
                playerId = playerId,
                lineupId = lineupId,
                position = FieldPosition.SUBSTITUTE.id,
                order = starterPositions.size + index + 1
            )
        }
        return starterPositions + substitutePositions
    }
}

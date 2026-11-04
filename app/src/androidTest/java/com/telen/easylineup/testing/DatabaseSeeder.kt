/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteAllData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Ids generated while seeding [TestFixtures], for tests that need to navigate straight to a
 * specific lineup/player rather than through the UI.
 */
data class SeededFixture(
    val teamId: Long,
    val playerIds: List<Long>,
    val tournamentId: Long,
    val lineupId: Long
)

/**
 * Seeds/clears the real Room database through the same repository ports production code uses
 * - acceptable for test infra (the constructor-injection/no-service-locator rule in CLAUDE.md
 * targets app ViewModels, not test harnesses). Inserts one row at a time (rather than the
 * app's own bulk `Insert*` use cases) specifically to get each row's generated id back, needed
 * to wire players into field positions/lineups.
 */
class DatabaseSeeder : KoinComponent {
    private val deleteAllData: DeleteAllData by inject()
    private val teamRepository: TeamRepository by inject()
    private val playerRepository: PlayerRepository by inject()
    private val tournamentRepository: TournamentRepository by inject()
    private val lineupRepository: LineupRepository by inject()
    private val playerFieldPositionRepository: PlayerFieldPositionRepository by inject()

    suspend fun clear() {
        deleteAllData().getOrThrow()
    }

    /**
     * Clears existing data, then inserts [TestFixtures]' team/roster/tournament/lineup with
     * the defense diagram + batting order already assigned.
     */
    suspend fun seedCoreFixture(): SeededFixture {
        clear()
        val teamId = teamRepository.insertTeam(TestFixtures.team())
        val playerIds = TestFixtures.players(teamId).map { playerRepository.insertPlayer(it) }
        val tournamentId = tournamentRepository.insertTournament(TestFixtures.tournament())
        val lineupId = lineupRepository.insertLineup(TestFixtures.lineup(teamId, tournamentId))
        playerFieldPositionRepository.insertPlayerFieldPositions(
            TestFixtures.playerFieldPositions(lineupId, playerIds)
        )
        return SeededFixture(teamId, playerIds, tournamentId, lineupId)
    }

    /**
     * Clears existing data, then inserts only [TestFixtures]' team/roster - no tournament, no
     * lineup. For screens that need to render their own "nothing built yet" empty state, which
     * [seedCoreFixture]'s always-populated lineup never exercises.
     */
    suspend fun seedTeamWithoutLineup(): SeededFixture {
        clear()
        val teamId = teamRepository.insertTeam(TestFixtures.team())
        val playerIds = TestFixtures.players(teamId).map { playerRepository.insertPlayer(it) }
        return SeededFixture(teamId, playerIds, tournamentId = 0L, lineupId = 0L)
    }

    /**
     * [seedCoreFixture] plus a second, roster-less, non-main team - for tests covering actually
     * switching teams via [com.telen.easylineup.team.swap.SwapTeamSheet], which needs more than
     * one team to even open.
     */
    suspend fun seedCoreFixtureWithSecondTeam(): SeededFixture {
        val fixture = seedCoreFixture()
        teamRepository.insertTeam(TestFixtures.secondTeam())
        return fixture
    }
}

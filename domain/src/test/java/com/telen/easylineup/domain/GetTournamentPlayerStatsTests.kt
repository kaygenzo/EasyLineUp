/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.StringResourcesProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentPlayerStats
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTournamentPlayerStatsTests {
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var stringResourcesProvider: StringResourcesProvider
    lateinit var getTournamentPlayerStats: GetTournamentPlayerStats
    lateinit var team: Team
    lateinit var tournament: Tournament

    @Before
    fun init() {
        runBlocking {
            team = Team(id = 1L, name = "toto", type = 0, main = true)
            tournament = Tournament(
                id = 5L,
                name = "Summer cup",
                createdAt = 1000L,
                startTime = 2000L,
                endTime = 3000L
            )
            getTournamentPlayerStats = GetTournamentPlayerStats(
                stringResourcesProvider,
                lineupDao,
                GetTeam(teamDao, testDispatcherProvider()),
                testDispatcherProvider()
            )

            Mockito.`when`(teamDao.getTeams()).thenReturn(listOf(team))
        }
    }

    @Test
    fun shouldPropagateRepositoryError() = runTest {
        val error = RuntimeException("db error")
        Mockito.`when`(lineupDao.getAllPlayerPositionsForTournament(tournament.id, team.id))
            .thenAnswer { throw error }

        val result = getTournamentPlayerStats(tournament)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == error.message)
    }

    @Test
    fun shouldAggregateTotalGamesAndPositionCountsPerPlayer() = runTest {
        Mockito.`when`(lineupDao.getAllPlayerPositionsForTournament(tournament.id, team.id))
            .thenReturn(
                listOf(
                    PlayerInLineup(
                        position = FieldPosition.SHORT_STOP.id,
                        playerName = "Jonas Alvarez",
                        playerId = 1L
                    ),
                    PlayerInLineup(
                        position = FieldPosition.SHORT_STOP.id,
                        playerName = "Jonas Alvarez",
                        playerId = 1L
                    ),
                    PlayerInLineup(
                        position = FieldPosition.SECOND_BASE.id,
                        playerName = "Jonas Alvarez",
                        playerId = 1L
                    ),
                    PlayerInLineup(
                        position = FieldPosition.SUBSTITUTE.id,
                        playerName = "Theo Nakamura",
                        playerId = 2L
                    )
                )
            )

        val result = getTournamentPlayerStats(tournament)

        assertTrue(result.isSuccess)
        val stats = result.getOrThrow()
        val alvarez = stats.first { it.playerId == 1L }
        assertEquals(3, alvarez.totalGames)
        assertEquals(2, alvarez.positionCounts[FieldPosition.SHORT_STOP])
        assertEquals(1, alvarez.positionCounts[FieldPosition.SECOND_BASE])

        val nakamura = stats.first { it.playerId == 2L }
        assertEquals(0, nakamura.totalGames)
        assertTrue(nakamura.positionCounts.isEmpty())
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import com.telen.easylineup.domain.usecases.GetTeam
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
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
internal class DeleteTournamentLineupsTests {
    @Mock lateinit var lineupsDao: LineupRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    @Mock lateinit var teamDao: TeamRepository
    private val transactionRunner = TestTransactionRunner()
    private lateinit var deleteTournament: DeleteTournamentLineups
    private val tournament = Tournament(id = 1L, name = "toto", createdAt = 1L, 2L, 3L, null)
    private val team = Team(id = 1L, name = "toto", main = true)

    private fun lineup(id: Long) = Lineup(
        id = id,
        teamId = team.id,
        tournamentId = tournament.id,
        eventTimeInMillis = 1L,
        strategy = TeamStrategy.STANDARD.id,
        extraHitters = 0
    )

    @Before
    fun init() {
        deleteTournament = DeleteTournamentLineups(
            lineupsDao,
            tournamentDao,
            GetTeam(teamDao, testDispatcherProvider()),
            transactionRunner,
            testDispatcherProvider()
        )
        runBlocking {
            Mockito.`when`(teamDao.getTeams()).thenReturn(listOf(team))
            Mockito.`when`(lineupsDao.getLineupsForTournament(tournament.id, team.id))
                .thenReturn(listOf(lineup(1L), lineup(2L)))
        }
    }

    @Test
    fun shouldDeleteOnlyLineupOfSpecificTournamentAndTeam() = runTest {
        Mockito.`when`(lineupsDao.hasLineupsForTournament(tournament.id)).thenReturn(true)

        val result = deleteTournament(tournament)

        assertTrue(result.isSuccess)
        verify(lineupsDao).deleteLineups(
            check {
                assertEquals(listOf(1L, 2L), it.map { lineup -> lineup.id })
            }
        )
    }

    @Test
    fun shouldDeleteTheTournamentOnceNoTeamHasALineupLeftInIt() = runTest {
        Mockito.`when`(lineupsDao.hasLineupsForTournament(tournament.id)).thenReturn(false)

        deleteTournament(tournament)

        verify(tournamentDao).deleteTournament(tournament)
        assertEquals(1, transactionRunner.transactions)
    }

    @Test
    fun shouldKeepTheTournamentWhileAnotherTeamStillPlaysInIt() = runTest {
        Mockito.`when`(lineupsDao.hasLineupsForTournament(tournament.id)).thenReturn(true)

        deleteTournament(tournament)

        verify(tournamentDao, never()).deleteTournament(any())
    }
}

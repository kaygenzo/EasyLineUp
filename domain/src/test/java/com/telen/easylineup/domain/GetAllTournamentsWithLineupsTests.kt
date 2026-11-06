/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TournamentWithLineup
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineups
import com.telen.easylineup.domain.usecases.GetTeam
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetAllTournamentsWithLineupsTests {
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getAllTournamentsWithLineups: GetAllTournamentsWithLineups
    lateinit var team: Team

    @Before
    fun init() {
        runBlocking {
        team = Team(id = 1L, name = "toto", main = true)
        getAllTournamentsWithLineups = GetAllTournamentsWithLineups(
            lineupDao,
            GetTeam(teamDao, testDispatcherProvider()),
            testDispatcherProvider()
        )

        Mockito.`when`(teamDao.getTeams()).thenReturn(listOf(team))
    }
    }

    @Test
    fun shouldReturnEmptyListWhenNoTournamentsForCurrentTeam() = runTest {
        Mockito.`when`(lineupDao.getAllTournamentsWithLineups("summer", team.id))
            .thenReturn(emptyList())

        val result = getAllTournamentsWithLineups("summer")

        assertTrue(result.isSuccess)
        Assert.assertTrue(result.getOrNull()?.isEmpty() ?: false)
        Mockito.verify(lineupDao).getAllTournamentsWithLineups("summer", team.id)
    }

    private fun row(
        tournamentId: Long,
        lineupId: Long,
        eventTime: Long = 0L,
        createdTime: Long = 0L
    ) = TournamentWithLineup(
        tournamentId = tournamentId,
        tournamentName = "Tournament $tournamentId",
        lineupName = "Lineup $lineupId",
        lineupId = lineupId,
        teamId = team.id,
        lineupEventTime = eventTime,
        lineupCreatedTime = createdTime
    )

    private suspend fun resultFor(vararg rows: TournamentWithLineup) = run {
        Mockito.`when`(lineupDao.getAllTournamentsWithLineups("", team.id))
            .thenReturn(rows.toList())
        getAllTournamentsWithLineups("").getOrThrow()
    }

    @Test
    fun shouldGroupLineupsUnderTheirTournamentKeepingTournamentOrder() = runTest {
        val result = resultFor(row(2L, 20L), row(1L, 10L), row(2L, 21L))

        assertEquals(listOf(2L, 1L), result.map { it.first.id })
        assertEquals(setOf(20L, 21L), result[0].second.map { it.id }.toSet())
        assertEquals(listOf(10L), result[1].second.map { it.id })
    }

    @Test
    fun shouldCollapseOneRowPerFieldPositionIntoASingleLineup() = runTest {
        val result = resultFor(row(1L, 10L), row(1L, 10L), row(1L, 10L))

        assertEquals(listOf(10L), result.single().second.map { it.id })
    }

    @Test
    fun shouldKeepATournamentWhoseRowHasNoLineup() = runTest {
        val result = resultFor(row(1L, 0L))

        assertEquals(1L, result.single().first.id)
        assertTrue(result.single().second.isEmpty())
    }

    @Test
    fun shouldSortLineupsByEventTimeFallingBackToCreationTimeMostRecentFirst() = runTest {
        val result = resultFor(
            row(1L, 10L, eventTime = 1_000L, createdTime = 9_000L),
            row(1L, 11L, eventTime = 0L, createdTime = 2_000L),
            row(1L, 12L, eventTime = 3_000L, createdTime = 1L)
        )

        assertEquals(listOf(12L, 11L, 10L), result.single().second.map { it.id })
    }

    @Test
    fun shouldMapTheRowsTournamentAndLineupFields() = runTest {
        val row = row(1L, 10L, eventTime = 1_000L).copy(
            tournamentCreatedAt = 5L,
            tournamentStartTime = 6L,
            tournamentEndTime = 7L,
            tournamentAddress = "Stadium",
            lineupMode = 1,
            lineupStrategy = TeamStrategy.SLOWPITCH.id,
            lineupExtraHittersSize = 2,
            roster = "1;2"
        )

        val (tournament, lineups) = resultFor(row).single()

        assertEquals("Tournament 1", tournament.name)
        assertEquals(5L, tournament.createdAt)
        assertEquals(6L, tournament.startTime)
        assertEquals(7L, tournament.endTime)
        assertEquals("Stadium", tournament.address)
        val lineup = lineups.single()
        assertEquals("Lineup 10", lineup.name)
        assertEquals(1L, lineup.tournamentId)
        assertEquals(team.id, lineup.teamId)
        assertEquals(1, lineup.mode)
        assertEquals(TeamStrategy.SLOWPITCH.id, lineup.strategy)
        assertEquals(2, lineup.extraHitters)
        assertEquals(1_000L, lineup.eventTimeInMillis)
        assertEquals("1;2", lineup.roster)
    }
}

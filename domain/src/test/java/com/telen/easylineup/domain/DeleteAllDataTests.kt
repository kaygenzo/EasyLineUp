/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteAllData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class DeleteAllDataTests {
    private val tournaments: MutableList<Tournament> = mutableListOf()
    private val teams: MutableList<Team> = mutableListOf()
    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    lateinit var deleteAllData: DeleteAllData

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        deleteAllData = DeleteAllData(teamDao, tournamentDao, testDispatcherProvider())

        tournaments.add(Tournament(1, "t1", 1L, 2L, 3L, null))
        tournaments.add(Tournament(2, "t2", 2L, 3L, 4L, null))
        tournaments.add(Tournament(3, "t3", 3L, 4L, 5L, null))

        teams.add(Team(1, "t1", null, TeamType.BASEBALL.id, true))
        teams.add(Team(2, "t2", null, TeamType.SOFTBALL.id, false))

        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(tournaments)
        Mockito.`when`(tournamentDao.deleteTournaments(tournaments)).thenReturn(Unit)
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(teams)
        Mockito.`when`(teamDao.deleteTeams(teams)).thenReturn(Unit)
    }
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotGetTournaments() = runTest {
        Mockito.`when`(tournamentDao.getTournaments()).thenAnswer { throw Exception() }
        val result = deleteAllData()
        assertTrue(result.isFailure)
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotDeleteTournaments() = runTest {
        Mockito.`when`(tournamentDao.deleteTournaments(tournaments))
            .thenAnswer { throw Exception() }
        val result = deleteAllData()
        assertTrue(result.isFailure)
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotGetTeams() = runTest {
        Mockito.`when`(teamDao.getTeamsRx()).thenAnswer { throw Exception() }
        val result = deleteAllData()
        assertTrue(result.isFailure)
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotDeleteTeams() = runTest {
        Mockito.`when`(teamDao.deleteTeams(teams)).thenAnswer { throw Exception() }
        val result = deleteAllData()
        assertTrue(result.isFailure)
    }

    @Test
    fun shouldSuccessfullyDeleteAllData() = runTest {
        val result = deleteAllData()
        assertTrue(result.isSuccess)
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.exceptions.NoTeamsLeftException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class DeleteTeamTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var deleteTeam: DeleteTeam
    lateinit var team: Team
    lateinit var team2: Team
    lateinit var team3: Team
    lateinit var teams: List<Team>

    private val transactionRunner = TestTransactionRunner()

    @Before
    fun init() {
        runBlocking {
        deleteTeam = DeleteTeam(teamDao, transactionRunner, testDispatcherProvider())
        team = Team(id = 1L, name = "toto", type = TeamType.BASEBALL.id, main = true)
        team2 = Team(id = 2L, name = "tata", type = TeamType.SOFTBALL.id, main = false)
        team3 = Team(id = 3L, name = "titi", type = TeamType.SOFTBALL.id, main = false)
        teams = mutableListOf(team2, team3)
        Mockito.`when`(teamDao.deleteTeam(team)).thenReturn(Unit)
        Mockito.`when`(teamDao.getTeams()).thenReturn(teams)
        Mockito.`when`(teamDao.updateTeam(team2.copy(main = true))).thenReturn(Unit)
    }
    }

    @Test
    fun shouldTriggerAnExceptionIfItWasTheOnlyTeamInDatabase() = runTest {
        Mockito.`when`(teamDao.getTeams()).thenReturn(mutableListOf())
        val result = deleteTeam(team)
        assertTrue(result.exceptionOrNull() is NoTeamsLeftException)
    }

    @Test
    fun shouldCommitTheDeleteOfTheLastTeamBeforeFailing() = runTest {
        Mockito.`when`(teamDao.getTeams()).thenReturn(mutableListOf())

        val result = deleteTeam(team)

        // The NoTeamsLeftException must surface after the transaction: thrown inside it, Room
        // would roll the delete back and the last team would never go away.
        assertTrue(result.exceptionOrNull() is NoTeamsLeftException)
        verify(teamDao).deleteTeam(team)
        Assert.assertEquals(1, transactionRunner.transactions)
        Assert.assertEquals(0, transactionRunner.rollbacks)
    }

    @Test
    fun shouldReassignMainTeamToTheFirstElement() = runTest {
        val result = deleteTeam(team)
        assertTrue(result.isSuccess)
        verify(teamDao).updateTeam(check {
            Assert.assertEquals(2L, it.id)
            Assert.assertEquals(true, it.main)
        })
        verify(teamDao, times(1)).updateTeam(any())
    }

    @Test
    fun shouldNotReassignMainTeam() = runTest {
        team = team.copy(main = false)
        team3 = team3.copy(main = true)
        val result = deleteTeam(team)
        assertTrue(result.isSuccess)
        verify(teamDao, never()).updateTeam(any())
    }

    @Test
    fun shouldDeleteSuccessfully() = runTest {
        val result = deleteTeam(team)
        assertTrue(result.isSuccess)
    }

    @Test
    fun shouldDeleteAndReassignMainTeamInOneTransaction() = runTest {
        val inTransaction = mutableListOf<Boolean>()
        Mockito.`when`(teamDao.deleteTeam(team)).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Unit
        }
        Mockito.`when`(teamDao.updateTeam(team2.copy(main = true))).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Unit
        }

        assertTrue(deleteTeam(team).isSuccess)
        Assert.assertEquals(listOf(true, true), inTransaction)
        Assert.assertEquals(1, transactionRunner.transactions)
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import com.telen.easylineup.domain.usecases.SaveTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
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

/**
 * `SaveTeam` also validates the name and marks the team as current - it absorbed that
 * orchestration from `TeamsInteractorImpl.saveTeam()` when the Interactor layer was removed.
 */
@RunWith(MockitoJUnitRunner::class)
internal class SaveTeamTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var saveTeam: SaveTeam
    lateinit var team: Team

    private val transactionRunner = TestTransactionRunner()

    @Before
    fun init() {
        runBlocking {
        saveTeam = SaveTeam(
            teamDao,
            CheckTeam(testDispatcherProvider()),
            SaveCurrentTeam(teamDao, testDispatcherProvider()),
            transactionRunner,
            testDispatcherProvider()
        )
        team = Team(id = 1L, name = "test", type = TeamType.BASEBALL.id, main = true)
        Mockito.`when`(teamDao.insertTeam(any())).thenReturn(2L)
        Mockito.`when`(teamDao.updateTeam(any())).thenReturn(Unit)
        Mockito.`when`(teamDao.getTeams()).thenReturn(listOf(team))
        Mockito.`when`(teamDao.updateTeams(any())).thenReturn(Unit)
    }
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() = runTest {
        team = team.copy(name = "")
        val result = saveTeam(team)
        assertTrue(result.exceptionOrNull() is NameEmptyException)
        verify(teamDao, never()).insertTeam(any())
        verify(teamDao, never()).updateTeam(any())
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsOnlyWhitespaces() = runTest {
        team = team.copy(name = "\n\t\r       ")
        val result = saveTeam(team)
        assertTrue(result.exceptionOrNull() is NameEmptyException)
    }

    @Test
    fun shouldUpdateTeamIfIdGreaterThatZero() = runTest {
        val result = saveTeam(team)
        assertTrue(result.isSuccess)
        verify(teamDao).updateTeam(any())
        verify(teamDao, never()).insertTeam(any())
        assertEquals(1L, result.getOrNull()?.id)
    }

    @Test
    fun shouldInsertTeamIfIdEqualsToZero() = runTest {
        team = team.copy(id = 0L)
        val result = saveTeam(team)
        assertTrue(result.isSuccess)
        verify(teamDao).insertTeam(any())
        verify(teamDao, never()).updateTeam(any())
        assertEquals(2L, result.getOrNull()?.id)
    }

    @Test
    fun shouldCorrectTeamTypeIfUnknown() = runTest {
        team = team.copy(type = TeamType.UNKNOWN.id)
        val result = saveTeam(team)
        assertTrue(result.isSuccess)
        assertEquals(TeamType.BASEBALL.id, result.getOrNull()?.type)
    }

    @Test
    fun shouldSetTeamAsCurrentAfterSaving() = runTest {
        val result = saveTeam(team)
        assertTrue(result.isSuccess)
        verify(teamDao).updateTeams(any())
    }

    @Test
    fun shouldSaveTeamAndMakeItCurrentInOneTransaction() = runTest {
        val inTransaction = mutableListOf<Boolean>()
        Mockito.`when`(teamDao.updateTeam(any())).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Unit
        }
        Mockito.`when`(teamDao.updateTeams(any())).thenAnswer {
            inTransaction.add(transactionRunner.isInTransaction)
            Unit
        }

        assertTrue(saveTeam(team).isSuccess)
        assertEquals(listOf(true, true), inTransaction)
        assertEquals(1, transactionRunner.transactions)
    }
}

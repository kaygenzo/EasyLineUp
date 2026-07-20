/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import com.telen.easylineup.domain.usecases.SaveTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
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

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        saveTeam = SaveTeam(
            teamDao,
            CheckTeam(testDispatcherProvider()),
            SaveCurrentTeam(teamDao, testDispatcherProvider()),
            testDispatcherProvider()
        )
        team = Team(id = 1L, name = "test", type = TeamType.BASEBALL.id, main = true)
        Mockito.`when`(teamDao.insertTeam(any())).thenReturn(Single.just(2L))
        Mockito.`when`(teamDao.updateTeam(any())).thenReturn(Completable.complete())
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(listOf(team)))
        Mockito.`when`(teamDao.updateTeams(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() = runTest {
        team.name = ""
        val result = saveTeam(team)
        assertTrue(result.exceptionOrNull() is NameEmptyException)
        verify(teamDao, never()).insertTeam(any())
        verify(teamDao, never()).updateTeam(any())
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsOnlyWhitespaces() = runTest {
        team.name = "\n\t\r       "
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
        team.id = 0L
        val result = saveTeam(team)
        assertTrue(result.isSuccess)
        verify(teamDao).insertTeam(any())
        verify(teamDao, never()).updateTeam(any())
        assertEquals(2L, result.getOrNull()?.id)
    }

    @Test
    fun shouldCorrectTeamTypeIfUnknown() = runTest {
        team.type = TeamType.UNKNOWN.id
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
}

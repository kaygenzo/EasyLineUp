/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.DeleteTeam
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class DeleteTeamTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var deleteTeam: DeleteTeam
    lateinit var team: Team
    lateinit var team2: Team
    lateinit var team3: Team
    lateinit var teams: List<Team>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deleteTeam = DeleteTeam(teamDao, testDispatcherProvider())
        team = Team(id = 1L, name = "toto", type = TeamType.BASEBALL.id, main = true)
        team2 = Team(id = 2L, name = "tata", type = TeamType.SOFTBALL.id, main = false)
        team3 = Team(id = 3L, name = "titi", type = TeamType.SOFTBALL.id, main = false)
        teams = mutableListOf(team2, team3)
        Mockito.`when`(teamDao.deleteTeam(team)).thenReturn(Completable.complete())
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
        Mockito.`when`(teamDao.updateTeam(team2)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnExceptionIfItWasTheOnlyTeamInDatabase() = runTest {
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(mutableListOf()))
        val result = deleteTeam(team)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
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
        team.main = false
        team3.main = true
        val result = deleteTeam(team)
        assertTrue(result.isSuccess)
        verify(teamDao, never()).updateTeam(any())
    }

    @Test
    fun shouldDeleteSuccessfully() = runTest {
        val result = deleteTeam(team)
        assertTrue(result.isSuccess)
    }
}

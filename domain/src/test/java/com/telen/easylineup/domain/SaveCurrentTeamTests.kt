/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import kotlinx.coroutines.runBlocking
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
internal class SaveCurrentTeamTests {
    private var teams: MutableList<Team> = mutableListOf()
    @Mock lateinit var teamDao: TeamRepository
    lateinit var saveCurrentTeam: SaveCurrentTeam
    lateinit var newTeam: Team

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        saveCurrentTeam = SaveCurrentTeam(teamDao, testDispatcherProvider())

        newTeam = Team(1, "toto", null, 0, true)
        teams.add(newTeam)
        teams.add(Team(2, "tata", null, 0, true))
        teams.add(Team(3, "titi", null, 0, true))

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(teams)
        Mockito.`when`(teamDao.updateTeams(teams)).thenReturn(Unit)
    }
    }

    @Test
    fun shouldChangeOfMainTeam() = runTest {
        val result = saveCurrentTeam(newTeam)

        assertTrue(result.isSuccess)
        verify(teamDao).updateTeams(check {
            Assert.assertEquals(true, it[0].main)
            Assert.assertEquals(false, it[1].main)
            Assert.assertEquals(false, it[2].main)
        })
    }
}

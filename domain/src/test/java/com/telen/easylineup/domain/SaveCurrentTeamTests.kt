/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.repository.TeamRepository
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
        saveCurrentTeam = SaveCurrentTeam(teamDao, testDispatcherProvider())

        newTeam = Team(1, "toto", null, 0, true)
        teams.add(newTeam)
        teams.add(Team(2, "tata", null, 0, true))
        teams.add(Team(3, "titi", null, 0, true))

        Mockito.`when`(teamDao.getTeams()).thenReturn(teams)
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

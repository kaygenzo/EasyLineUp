/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTeamTests {
    private var teams: MutableList<Team> = mutableListOf()
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getTeam: GetTeam

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        getTeam = GetTeam(teamDao, testDispatcherProvider())

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(teams)
    }
    }

    @Test
    fun shouldGetTheFirstTeam() = runTest {
        teams.add(Team(1, "toto", null, 0, true))
        teams.add(Team(2, "tata", null, 0, false))
        teams.add(Team(3, "titi", null, 0, false))

        val result = getTeam()

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(teams[0], result.getOrNull())
    }

    @Test
    fun shouldGetTheLastTeam() = runTest {
        teams.add(Team(1, "toto", null, 0, false))
        teams.add(Team(2, "tata", null, 0, false))
        teams.add(Team(3, "titi", null, 0, true))

        val result = getTeam()

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(teams[2], result.getOrNull())
    }
}

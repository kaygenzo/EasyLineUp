/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetAllTeams
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetAllTeamsTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getAllTeams: GetAllTeams
    lateinit var teams: MutableList<Team>

    @Before
    fun init() {
        runBlocking {
        getAllTeams = GetAllTeams(teamDao, testDispatcherProvider())

        teams = mutableListOf()
        teams.add(Team(1, "toto", null, 0, true))
        teams.add(Team(2, "tata", null, 0, true))
        teams.add(Team(3, "titi", null, 0, true))
        Mockito.`when`(teamDao.getTeams()).thenReturn(teams)
    }
    }

    @Test
    fun shouldDeleteTournament() = runTest {
        val result = getAllTeams()

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(teams, result.getOrNull())
    }
}

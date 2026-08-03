/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineups
import com.telen.easylineup.domain.usecases.GetTeam
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
}

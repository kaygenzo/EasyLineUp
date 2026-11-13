/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.ObserveCurrentTeam
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObserveCurrentTeamTests {
    @Mock lateinit var teamDao: TeamRepository

    private val hawks = Team(id = 1L, name = "Hawks", main = true)
    private val tigers = Team(id = 2L, name = "Tigers")

    @Test
    fun shouldEmitTheMainTeamThenFollowASwitch() = runTest {
        Mockito.`when`(teamDao.observeTeams()).thenReturn(
            flowOf(
                listOf(hawks, tigers),
                listOf(hawks.copy(main = false), tigers.copy(main = true))
            )
        )

        val teams = ObserveCurrentTeam(teamDao)().toList()

        assertEquals(listOf(1L, 2L), teams.map { it?.id })
    }

    @Test
    fun shouldNotEmitAgainWhenAnotherTeamChanges() = runTest {
        Mockito.`when`(teamDao.observeTeams()).thenReturn(
            flowOf(listOf(hawks, tigers), listOf(hawks, tigers.copy(name = "Lions")))
        )

        val teams = ObserveCurrentTeam(teamDao)().toList()

        assertEquals(listOf(hawks), teams)
    }

    @Test
    fun shouldEmitNullWithoutAnyTeam() = runTest {
        Mockito.`when`(teamDao.observeTeams()).thenReturn(flowOf(emptyList()))

        assertEquals(listOf<Team?>(null), ObserveCurrentTeam(teamDao)().toList())
    }
}

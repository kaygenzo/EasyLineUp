/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.ObserveTeams
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObserveTeamsTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var observeTeams: ObserveTeams

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observeTeams = ObserveTeams(teamDao)
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val teams = listOf(Team(1L, "toto"))
        Mockito.`when`(teamDao.getTeams()).thenReturn(flowOf(teams))

        val result = observeTeams().toList()

        assertEquals(listOf(teams), result)
    }
}

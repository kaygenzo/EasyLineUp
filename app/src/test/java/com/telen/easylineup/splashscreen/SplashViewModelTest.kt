/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.testDispatcherProvider
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

@RunWith(MockitoJUnitRunner::class)
internal class SplashViewModelTest {
    @Mock
    lateinit var teamRepository: TeamRepository

    lateinit var viewModel: SplashViewModel

    @Before
    fun setup() {
        viewModel = SplashViewModel(GetTeam(teamRepository, testDispatcherProvider()))
    }

    @Test
    fun shouldReturnTheMainTeamWhenOneExists() = runTest {
        val team = Team(id = 1L, name = "Panthers", main = true)
        runBlocking {
            Mockito.`when`(teamRepository.getTeams()).thenReturn(listOf(team))
        }

        val result = viewModel.getTeam()

        assertTrue(result.isSuccess)
        assertEquals(team, result.getOrNull())
    }

    @Test
    fun shouldFailWhenNoMainTeamExists() = runTest {
        runBlocking {
            Mockito.`when`(teamRepository.getTeams()).thenReturn(emptyList())
        }

        val result = viewModel.getTeam()

        assertTrue(result.isFailure)
    }
}

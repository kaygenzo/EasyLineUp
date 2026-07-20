/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTeamPhones
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTeamPhonesTests {
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getTeamPhones: GetTeamPhones

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        getTeamPhones = GetTeamPhones(
            GetPlayers(playerDao, GetTeam(teamDao, testDispatcherProvider()), testDispatcherProvider()),
            testDispatcherProvider()
        )
        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(listOf(Team(id = 1L, name = "Panthers", main = true)))
    }
    }

    @Test
    fun shouldFilterOutPlayersWithoutPhone() = runTest {
        val withPhone = Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L, phone = "0102030405")
        val withoutPhone = Player(id = 2L, teamId = 1L, name = "Titi", shirtNumber = 2, licenseNumber = 2L, phone = null)
        Mockito.`when`(playerDao.getPlayersByTeamId(1L))
            .thenReturn(listOf(withPhone, withoutPhone))

        val result = getTeamPhones()

        assertTrue(result.isSuccess)
        assertEquals(listOf("0102030405"), result.getOrNull())
    }
}

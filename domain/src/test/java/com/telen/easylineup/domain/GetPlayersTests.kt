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
import io.reactivex.rxjava3.core.Single
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
internal class GetPlayersTests {
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getPlayers: GetPlayers
    lateinit var players: MutableList<Player>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getPlayers = GetPlayers(playerDao, GetTeam(teamDao, testSchedulersProvider()), testDispatcherProvider())

        val player1 = Player(id = 1L, teamId = 1L, name = "toto", shirtNumber = 1, licenseNumber = 1, image = null,
            positions = 1)
        val player2 = Player(id = 2L, teamId = 1L, name = "tata", shirtNumber = 2, licenseNumber = 2, image = null,
            positions = 1)

        players = arrayListOf(player1, player2)

        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(Single.just(listOf(Team(id = 1L, name = "Panthers", main = true))))
        Mockito.`when`(playerDao.getPlayersByTeamId(1L)).thenReturn(Single.just(players))
    }

    @Test
    fun shouldGetPlayersTeam() = runTest {
        val result = getPlayers()
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(players[0], result.getOrNull()?.get(0))
        Assert.assertEquals(players[1], result.getOrNull()?.get(1))
    }
}

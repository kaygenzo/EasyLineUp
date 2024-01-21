/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.GetPlayers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
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
    val observer: TestObserver<GetPlayers.ResponseValue> = TestObserver()
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var getPlayers: GetPlayers
    lateinit var players: MutableList<Player>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getPlayers = GetPlayers(playerDao)

        val player1 = Player(id = 1L, teamId = 1L, name = "toto", shirtNumber = 1, licenseNumber = 1, image = null,
            positions = 1)
        val player2 = Player(id = 2L, teamId = 1L, name = "tata", shirtNumber = 2, licenseNumber = 2, image = null,
            positions = 1)

        players = arrayListOf(player1, player2)

        Mockito.`when`(playerDao.getPlayersByTeamId(1L)).thenReturn(Single.just(players))
    }

    @Test
    fun shouldGetPlayersTeam() {
        getPlayers.executeUseCase(GetPlayers.RequestValues(1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(players[0], observer.values().first().players[0])
        Assert.assertEquals(players[1], observer.values().first().players[1])
    }
}

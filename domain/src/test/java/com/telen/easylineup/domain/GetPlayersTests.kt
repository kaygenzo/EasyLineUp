package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.repository.dao.PlayerDao
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Team
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class GetPlayersTests {

    @Mock lateinit var playerDao: PlayerDao
    lateinit var mGetPlayers: GetPlayers

    lateinit var players: MutableList<Player>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetPlayers = GetPlayers(playerDao)

        val player1 = Player(id = 1L, teamId = 1L, name = "toto", shirtNumber =  1, licenseNumber = 1, image = null, positions = 1)
        val player2 = Player(id = 2L, teamId = 1L, name = "tata", shirtNumber =  2, licenseNumber = 2, image = null, positions = 1)

        players = arrayListOf(player1, player2)

        Mockito.`when`(playerDao.getPlayers(1L)).thenReturn(Single.just(players))
    }

    @Test
    fun shouldGetPlayersTeam() {
        val observer = TestObserver<GetPlayers.ResponseValue>()
        mGetPlayers.executeUseCase(GetPlayers.RequestValues(1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(players[0], observer.values().first().players[0])
        Assert.assertEquals(players[1], observer.values().first().players[1])
    }
}
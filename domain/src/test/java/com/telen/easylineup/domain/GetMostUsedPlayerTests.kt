package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerGamesCount
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.KEY_DATA_IMAGE
import com.telen.easylineup.domain.model.tiles.KEY_DATA_MATCH_PLAYED
import com.telen.easylineup.domain.model.tiles.KEY_DATA_NAME
import com.telen.easylineup.domain.model.tiles.KEY_DATA_SHIRT_NUMBER
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.GetMostUsedPlayer
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
internal class GetMostUsedPlayerTests {

    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var playerFieldPositionsDao: PlayerFieldPositionRepository
    lateinit var mGetMostUsedPlayer: GetMostUsedPlayer

    lateinit var mPlayerGameCounts: MutableList<PlayerGamesCount>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetMostUsedPlayer = GetMostUsedPlayer(playerFieldPositionsDao, playerDao)

        val player1 = PlayerGamesCount(playerID = 2L, size = 4)
        val player2 = PlayerGamesCount(playerID = 4L, size = 3)
        val player3 = PlayerGamesCount(playerID = 1L, size = 2)
        val player4 = PlayerGamesCount(playerID = 3L, size = 1)

        mPlayerGameCounts = arrayListOf(player1, player2, player3, player4)

        Mockito.`when`(playerFieldPositionsDao.getMostUsedPlayers(1L)).thenReturn(Single.just(mPlayerGameCounts))
        Mockito.`when`(playerDao.getPlayerByIdAsSingle(2L)).thenReturn(Single.just(Player(id = 2L, name = "toto", shirtNumber = 42, licenseNumber = 2L, teamId = 1L, image = "imagePath")))
    }

    @Test
    fun shouldReturnNullElementIfListIsEmpty() {
        Mockito.`when`(playerFieldPositionsDao.getMostUsedPlayers(1L)).thenReturn(Single.just(mutableListOf()))
        val observer = TestObserver<GetMostUsedPlayer.ResponseValue>()
        mGetMostUsedPlayer.executeUseCase(GetMostUsedPlayer.RequestValues(Team(id = 1L))).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(null, observer.values().first().data)
    }

    @Test
    fun shouldReturnDataWithFirstPlayerIfListIsNotEmpty() {
        val observer = TestObserver<GetMostUsedPlayer.ResponseValue>()
        mGetMostUsedPlayer.executeUseCase(GetMostUsedPlayer.RequestValues(Team(id = 1L))).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals("imagePath", observer.values().first().data?.getData()?.get(KEY_DATA_IMAGE))
        Assert.assertEquals("toto", observer.values().first().data?.getData()?.get(KEY_DATA_NAME))
        Assert.assertEquals(42, observer.values().first().data?.getData()?.get(KEY_DATA_SHIRT_NUMBER))
        Assert.assertEquals(4, observer.values().first().data?.getData()?.get(KEY_DATA_MATCH_PLAYED))
    }

    @Test
    fun shouldNotInsertImageIfPlayerImageIsNull() {
        Mockito.`when`(playerDao.getPlayerByIdAsSingle(2L)).thenReturn(Single.just(Player(id = 2L, name = "toto", shirtNumber = 42, licenseNumber = 2L, teamId = 1L)))
        val observer = TestObserver<GetMostUsedPlayer.ResponseValue>()
        mGetMostUsedPlayer.executeUseCase(GetMostUsedPlayer.RequestValues(Team(id = 1L))).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(null, observer.values().first().data?.getData()?.get(KEY_DATA_IMAGE))
    }
}
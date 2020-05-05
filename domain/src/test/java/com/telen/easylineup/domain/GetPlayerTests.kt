package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.repository.dao.PlayerDao
import com.telen.easylineup.repository.model.Player
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
import java.lang.Exception
import java.lang.IllegalArgumentException


@RunWith(MockitoJUnitRunner::class)
class GetPlayerTests {

    @Mock lateinit var playerDao: PlayerDao
    lateinit var mGetPlayer: GetPlayer
    lateinit var player: Player

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetPlayer = GetPlayer(playerDao)

        player = Player(id = 1L, teamId = 1L, name = "toto", shirtNumber =  1, licenseNumber = 1, image = null, positions = 1)

        Mockito.`when`(playerDao.getPlayerByIdAsSingle(1L)).thenReturn(Single.just(player))
        Mockito.`when`(playerDao.getPlayerByIdAsSingle(2L)).thenReturn(Single.error(Exception()))
    }

    @Test
    fun shouldGetPlayerIfValidId() {
        val observer = TestObserver<GetPlayer.ResponseValue>()
        mGetPlayer.executeUseCase(GetPlayer.RequestValues(1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(player, observer.values().first().player)
    }

    @Test
    fun shouldTriggerAnExceptionIfUnknownId() {
        val observer = TestObserver<GetPlayer.ResponseValue>()
        mGetPlayer.executeUseCase(GetPlayer.RequestValues(2L)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfIdIsNull() {
        val observer = TestObserver<GetPlayer.ResponseValue>()
        mGetPlayer.executeUseCase(GetPlayer.RequestValues(null)).subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }
}
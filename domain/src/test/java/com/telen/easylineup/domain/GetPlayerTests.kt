package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.domain.usecases.exceptions.NotExistingPlayer
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
internal class GetPlayerTests {

    @Mock lateinit var playerDao: PlayerRepository
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
    fun shouldTriggerAnExceptionIfIdIsLessOrEqualsTo0() {
        val observer = TestObserver<GetPlayer.ResponseValue>()
        mGetPlayer.executeUseCase(GetPlayer.RequestValues(0L)).subscribe(observer)
        observer.await()
        observer.assertError(NotExistingPlayer::class.java)
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
/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.domain.usecases.exceptions.NotExistingPlayerException
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
    val observer: TestObserver<GetPlayer.ResponseValue> = TestObserver()

    @Mock
    lateinit var playerDao: PlayerRepository
    lateinit var getPlayer: GetPlayer
    lateinit var player: Player

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getPlayer = GetPlayer(playerDao)

        player = Player(
            id = 1L,
            teamId = 1L,
            name = "toto",
            shirtNumber = 1,
            licenseNumber = 1,
            image = null,
            positions = 1
        )

        Mockito.`when`(playerDao.getPlayerByIdAsSingle(1L)).thenReturn(Single.just(player))
        Mockito.`when`(playerDao.getPlayerByIdAsSingle(2L)).thenReturn(Single.error(Exception()))
    }

    @Test
    fun shouldGetPlayerIfValidId() {
        getPlayer.executeUseCase(GetPlayer.RequestValues(1L)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(player, observer.values().first().player)
    }

    @Test
    fun shouldTriggerAnExceptionIfIdIsLessOrEqualsTo0() {
        getPlayer.executeUseCase(GetPlayer.RequestValues(0L)).subscribe(observer)
        observer.await()
        observer.assertError(NotExistingPlayerException::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfUnknownId() {
        getPlayer.executeUseCase(GetPlayer.RequestValues(2L)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfIdIsNull() {
        getPlayer.executeUseCase(GetPlayer.RequestValues(null)).subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }
}

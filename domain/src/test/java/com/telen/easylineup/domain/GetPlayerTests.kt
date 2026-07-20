/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.domain.usecases.exceptions.NotExistingPlayerException
import io.reactivex.rxjava3.core.Single
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
internal class GetPlayerTests {
    @Mock
    lateinit var playerDao: PlayerRepository
    lateinit var getPlayer: GetPlayer
    lateinit var player: Player

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getPlayer = GetPlayer(playerDao, testDispatcherProvider())

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
    fun shouldGetPlayerIfValidId() = runTest {
        val result = getPlayer(1L)
        assertTrue(result.isSuccess)
        assertEquals(player, result.getOrNull())
    }

    @Test
    fun shouldTriggerAnExceptionIfIdIsLessOrEqualsTo0() = runTest {
        val result = getPlayer(0L)
        assertTrue(result.exceptionOrNull() is NotExistingPlayerException)
    }

    @Test
    fun shouldTriggerAnExceptionIfUnknownId() = runTest {
        val result = getPlayer(2L)
        assertTrue(result.isFailure)
    }

    @Test
    fun shouldTriggerAnExceptionIfIdIsNull() = runTest {
        val result = getPlayer(null)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}

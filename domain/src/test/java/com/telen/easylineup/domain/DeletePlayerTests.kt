/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.verify
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.GetPlayer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerTests {
    @Mock
    lateinit var playerDao: PlayerRepository
    lateinit var deletePlayer: DeletePlayer
    private lateinit var player: Player

    @Before
    fun init() {
        runBlocking {
        deletePlayer = DeletePlayer(
            playerDao,
            GetPlayer(playerDao, testDispatcherProvider()),
            testDispatcherProvider()
        )

        player = Player(
            id = 1L,
            teamId = 1L,
            name = "toto",
            shirtNumber = 1,
            licenseNumber = 1,
            image = null,
            positions = 1
        )

        Mockito.`when`(playerDao.getPlayerById(1L)).thenReturn(player)
        Mockito.`when`(playerDao.deletePlayer(player)).thenReturn(Unit)
    }
    }

    @Test
    fun shouldDeletePlayer() = runTest {
        val result = deletePlayer(1L)
        assertTrue(result.isSuccess)
        verify(playerDao).deletePlayer(player)
    }
}

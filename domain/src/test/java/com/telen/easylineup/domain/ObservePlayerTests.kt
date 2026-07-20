/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.ObservePlayer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObservePlayerTests {
    @Mock lateinit var playerDao: PlayerRepository
    lateinit var observePlayer: ObservePlayer

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observePlayer = ObservePlayer(playerDao)
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val player = Player(teamId = 0, name = "", shirtNumber = 0, licenseNumber = 0)
        Mockito.`when`(playerDao.getPlayerById(1L)).thenReturn(flowOf(player))

        val result = observePlayer(1L).toList()

        assertEquals(listOf(player), result)
    }
}

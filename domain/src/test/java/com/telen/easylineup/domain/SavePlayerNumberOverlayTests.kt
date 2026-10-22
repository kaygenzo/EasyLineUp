/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.ports.repository.PlayerNumberOverlayRepository
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerNumberOverlayTests {
    @Mock lateinit var playerNumberOverlayRepository: PlayerNumberOverlayRepository
    lateinit var savePlayerNumberOverlay: SavePlayerNumberOverlay

    private fun player(id: Long, shirtNumber: Int) =
        Player(id = id, teamId = 1L, name = "p$id", shirtNumber = shirtNumber, licenseNumber = id)

    @Before
    fun init() {
        runBlocking {
        savePlayerNumberOverlay = SavePlayerNumberOverlay(playerNumberOverlayRepository, testDispatcherProvider())
        Mockito.`when`(playerNumberOverlayRepository.deletePlayerNumberOverlays(any())).thenReturn(Unit)
        Mockito.`when`(playerNumberOverlayRepository.updatePlayerNumberOverlays(any())).thenReturn(Unit)
        Mockito.`when`(playerNumberOverlayRepository.insertPlayerNumberOverlays(any())).thenReturn(Unit)
    }
    }

    @Test
    fun shouldCreateNewOverlayWhenNumberDiffersAndOverlayIsNew() = runTest {
        val overlay = PlayerNumberOverlay(id = 0L, lineupId = 1L, playerId = 1L, number = 99)
        val item = RosterPlayerStatus(
            player(1L, shirtNumber = 1),
            status = true,
            playerNumberOverlay = overlay
        )

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<PlayerNumberOverlay>>()
        Mockito.verify(playerNumberOverlayRepository).insertPlayerNumberOverlays(captor.capture())
        assertEquals(listOf(overlay), captor.firstValue)
        Mockito.verify(playerNumberOverlayRepository).updatePlayerNumberOverlays(emptyList())
        Mockito.verify(playerNumberOverlayRepository).deletePlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldUpdateExistingOverlayWhenNumberDiffersAndOverlayAlreadyExists() = runTest {
        val overlay = PlayerNumberOverlay(id = 5L, lineupId = 1L, playerId = 1L, number = 99)
        val item = RosterPlayerStatus(
            player(1L, shirtNumber = 1),
            status = true,
            playerNumberOverlay = overlay
        )

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<PlayerNumberOverlay>>()
        Mockito.verify(playerNumberOverlayRepository).updatePlayerNumberOverlays(captor.capture())
        assertEquals(listOf(overlay), captor.firstValue)
        Mockito.verify(playerNumberOverlayRepository).insertPlayerNumberOverlays(emptyList())
        Mockito.verify(playerNumberOverlayRepository).deletePlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldDeleteExistingOverlayWhenNumberMatchesShirtNumber() = runTest {
        val overlay = PlayerNumberOverlay(id = 5L, lineupId = 1L, playerId = 1L, number = 1)
        val item = RosterPlayerStatus(
            player(1L, shirtNumber = 1),
            status = true,
            playerNumberOverlay = overlay
        )

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<PlayerNumberOverlay>>()
        Mockito.verify(playerNumberOverlayRepository).deletePlayerNumberOverlays(captor.capture())
        assertEquals(listOf(overlay), captor.firstValue)
        Mockito.verify(playerNumberOverlayRepository).insertPlayerNumberOverlays(emptyList())
        Mockito.verify(playerNumberOverlayRepository).updatePlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldDoNothingWhenNumberMatchesShirtNumberAndOverlayIsNotPersisted() = runTest {
        val overlay = PlayerNumberOverlay(id = 0L, lineupId = 1L, playerId = 1L, number = 1)
        val item = RosterPlayerStatus(
            player(1L, shirtNumber = 1),
            status = true,
            playerNumberOverlay = overlay
        )

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        Mockito.verify(playerNumberOverlayRepository).deletePlayerNumberOverlays(emptyList())
        Mockito.verify(playerNumberOverlayRepository).updatePlayerNumberOverlays(emptyList())
        Mockito.verify(playerNumberOverlayRepository).insertPlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldIgnoreItemsWithNullOverlay() = runTest {
        val item = RosterPlayerStatus(
            player(1L, shirtNumber = 1),
            status = true,
            playerNumberOverlay = null
        )

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        Mockito.verify(playerNumberOverlayRepository).deletePlayerNumberOverlays(emptyList())
        Mockito.verify(playerNumberOverlayRepository).updatePlayerNumberOverlays(emptyList())
        Mockito.verify(playerNumberOverlayRepository).insertPlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldPropagateErrorFromRepository() = runTest {
        val exception = Exception("db error")
        Mockito.`when`(playerNumberOverlayRepository.deletePlayerNumberOverlays(any())).thenAnswer { throw exception }
        val item = RosterPlayerStatus(
            player(1L, shirtNumber = 1),
            status = true,
            playerNumberOverlay = null
        )

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isFailure)
        // coroutines may clone the exception across the withContext boundary to recover the
        // stack trace, so compare by message/type rather than reference/equals.
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }
}

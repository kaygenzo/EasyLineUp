/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import io.reactivex.rxjava3.core.Completable
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
internal class SavePlayerNumberOverlayTests {
    @Mock lateinit var playerRepository: PlayerRepository
    lateinit var savePlayerNumberOverlay: SavePlayerNumberOverlay

    private fun player(id: Long, shirtNumber: Int) =
        Player(id = id, teamId = 1L, name = "p$id", shirtNumber = shirtNumber, licenseNumber = id)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        savePlayerNumberOverlay = SavePlayerNumberOverlay(playerRepository, testDispatcherProvider())
        Mockito.`when`(playerRepository.deletePlayerNumberOverlays(any())).thenReturn(Completable.complete())
        Mockito.`when`(playerRepository.updatePlayerNumberOverlays(any())).thenReturn(Completable.complete())
        Mockito.`when`(playerRepository.createPlayerNumberOverlays(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldCreateNewOverlayWhenNumberDiffersAndOverlayIsNew() = runTest {
        val overlay = PlayerNumberOverlay(id = 0L, lineupId = 1L, playerId = 1L, number = 99)
        val item = RosterItem(player(1L, shirtNumber = 1), selected = true, playerNumberOverlay = overlay)

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<PlayerNumberOverlay>>()
        Mockito.verify(playerRepository).createPlayerNumberOverlays(captor.capture())
        assertEquals(listOf(overlay), captor.firstValue)
        Mockito.verify(playerRepository).updatePlayerNumberOverlays(emptyList())
        Mockito.verify(playerRepository).deletePlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldUpdateExistingOverlayWhenNumberDiffersAndOverlayAlreadyExists() = runTest {
        val overlay = PlayerNumberOverlay(id = 5L, lineupId = 1L, playerId = 1L, number = 99)
        val item = RosterItem(player(1L, shirtNumber = 1), selected = true, playerNumberOverlay = overlay)

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<PlayerNumberOverlay>>()
        Mockito.verify(playerRepository).updatePlayerNumberOverlays(captor.capture())
        assertEquals(listOf(overlay), captor.firstValue)
        Mockito.verify(playerRepository).createPlayerNumberOverlays(emptyList())
        Mockito.verify(playerRepository).deletePlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldDeleteExistingOverlayWhenNumberMatchesShirtNumber() = runTest {
        val overlay = PlayerNumberOverlay(id = 5L, lineupId = 1L, playerId = 1L, number = 1)
        val item = RosterItem(player(1L, shirtNumber = 1), selected = true, playerNumberOverlay = overlay)

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        val captor = argumentCaptor<List<PlayerNumberOverlay>>()
        Mockito.verify(playerRepository).deletePlayerNumberOverlays(captor.capture())
        assertEquals(listOf(overlay), captor.firstValue)
        Mockito.verify(playerRepository).createPlayerNumberOverlays(emptyList())
        Mockito.verify(playerRepository).updatePlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldDoNothingWhenNumberMatchesShirtNumberAndOverlayIsNotPersisted() = runTest {
        val overlay = PlayerNumberOverlay(id = 0L, lineupId = 1L, playerId = 1L, number = 1)
        val item = RosterItem(player(1L, shirtNumber = 1), selected = true, playerNumberOverlay = overlay)

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        Mockito.verify(playerRepository).deletePlayerNumberOverlays(emptyList())
        Mockito.verify(playerRepository).updatePlayerNumberOverlays(emptyList())
        Mockito.verify(playerRepository).createPlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldIgnoreItemsWithNullOverlay() = runTest {
        val item = RosterItem(player(1L, shirtNumber = 1), selected = true, playerNumberOverlay = null)

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isSuccess)
        Mockito.verify(playerRepository).deletePlayerNumberOverlays(emptyList())
        Mockito.verify(playerRepository).updatePlayerNumberOverlays(emptyList())
        Mockito.verify(playerRepository).createPlayerNumberOverlays(emptyList())
    }

    @Test
    fun shouldPropagateErrorFromRepository() = runTest {
        val exception = Exception("db error")
        Mockito.`when`(playerRepository.deletePlayerNumberOverlays(any())).thenReturn(Completable.error(exception))
        val item = RosterItem(player(1L, shirtNumber = 1), selected = true, playerNumberOverlay = null)

        val result = savePlayerNumberOverlay(listOf(item))

        assertTrue(result.isFailure)
        // coroutines may clone the exception across the withContext boundary to recover the
        // stack trace, so compare by message/type rather than reference/equals.
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }
}

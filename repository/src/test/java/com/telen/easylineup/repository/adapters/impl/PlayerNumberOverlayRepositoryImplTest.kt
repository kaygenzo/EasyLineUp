/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.repository.dao.PlayerDao
import com.telen.easylineup.repository.dao.PlayerNumberOverlayDao
import com.telen.easylineup.repository.model.RoomPlayerNumberOverlay
import com.telen.easylineup.repository.model.RoomShirtNumberEntry
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

/**
 * Unit tests for [PlayerNumberOverlayRepositoryImpl]: verifies it maps domain<->Room correctly
 * and delegates to [PlayerNumberOverlayDao]/[PlayerDao] - both mocked, so this never touches a
 * real database. [getShirtNumberFromNumberOverlays] is the one method here routed through
 * [PlayerDao] rather than [PlayerNumberOverlayDao], worth pinning down explicitly.
 */
@RunWith(MockitoJUnitRunner::class)
internal class PlayerNumberOverlayRepositoryImplTest {
    @Mock
    lateinit var numberOverlayDao: PlayerNumberOverlayDao

    @Mock
    lateinit var playerDao: PlayerDao

    private lateinit var repository: PlayerNumberOverlayRepositoryImpl

    @Before
    fun setup() {
        repository = PlayerNumberOverlayRepositoryImpl(numberOverlayDao, playerDao)
    }

    private fun overlay(id: Long = 0, lineupId: Long = 1L, playerId: Long = 1L, number: Int = 7) =
        PlayerNumberOverlay(id = id, lineupId = lineupId, playerId = playerId, number = number)

    @Test
    fun shouldObservePlayersNumberOverlayMappedToDomain() = runTest {
        val rooms = listOf(RoomPlayerNumberOverlay(id = 1L, lineupId = 5L, number = 7))
        Mockito.`when`(numberOverlayDao.observePlayerNumberOverlays(5L)).thenReturn(flowOf(rooms))

        val result = repository.observePlayersNumberOverlay(5L).toList()

        assertEquals(listOf(7), result.first().map { it.number })
    }

    @Test
    fun shouldGetPlayersNumberOverlayMappedToDomain() = runTest {
        val rooms = listOf(RoomPlayerNumberOverlay(id = 1L, lineupId = 5L, number = 7))
        Mockito.`when`(numberOverlayDao.getPlayerNumberOverlays(5L)).thenReturn(rooms)

        val result = repository.getPlayersNumberOverlay(5L)

        assertEquals(listOf(7), result.map { it.number })
    }

    @Test
    fun shouldDeletePlayerNumberOverlaysMappedToRoom() = runTest {
        repository.deletePlayerNumberOverlays(listOf(overlay(id = 1L), overlay(id = 2L)))

        verify(numberOverlayDao).deletePlayerNumberOverlays(argThat { size == 2 })
    }

    @Test
    fun shouldUpdatePlayerNumberOverlaysMappedToRoom() = runTest {
        repository.updatePlayerNumberOverlays(listOf(overlay(id = 1L, number = 9)))

        verify(numberOverlayDao).updatePlayerNumberOverlays(argThat { first().number == 9 })
    }

    @Test
    fun shouldUpdatePlayerNumberOverlayMappedToRoom() = runTest {
        repository.updatePlayerNumberOverlay(overlay(id = 1L, number = 9))

        verify(numberOverlayDao).updatePlayerNumberOverlay(argThat { number == 9 })
    }

    @Test
    fun shouldInsertPlayerNumberOverlaysMappedToRoom() = runTest {
        repository.insertPlayerNumberOverlays(
            listOf(overlay(playerId = 1L), overlay(playerId = 2L))
        )

        verify(numberOverlayDao).insertPlayerNumberOverlays(argThat { size == 2 })
    }

    @Test
    fun shouldInsertPlayerNumberOverlayMappedToRoom() = runTest {
        repository.insertPlayerNumberOverlay(overlay(playerId = 3L))

        verify(numberOverlayDao).insertPlayerNumberOverlay(argThat { playerId == 3L })
    }

    @Test
    fun shouldGetShirtNumberFromNumberOverlaysRoutedThroughPlayerDao() = runTest {
        val rooms = listOf(
            RoomShirtNumberEntry(
                number = 7,
                playerName = "Alice",
                playerId = 1L,
                eventTime = 1L,
                createdAt = 1L,
                lineupId = 1L,
                lineupName = "L"
            )
        )
        Mockito.`when`(playerDao.getShirtNumberHistoryFromOverlays(1L, 7)).thenReturn(rooms)

        val result = repository.getShirtNumberFromNumberOverlays(1L, 7)

        assertEquals(listOf("Alice"), result.map { it.playerName })
        verifyNoInteractions(numberOverlayDao)
    }

    @Test
    fun shouldGetShirtNumberOverlayMappedToDomain() = runTest {
        val room = RoomPlayerNumberOverlay(id = 1L, playerId = 2L, lineupId = 5L)
        Mockito.`when`(numberOverlayDao.getShirtNumberOverlay(2L, 5L)).thenReturn(room)

        val result = repository.getShirtNumberOverlay(2L, 5L)

        assertEquals(1L, result?.id)
    }

    @Test
    fun shouldReturnNullWhenNoShirtNumberOverlay() = runTest {
        Mockito.`when`(numberOverlayDao.getShirtNumberOverlay(2L, 5L)).thenReturn(null)

        val result = repository.getShirtNumberOverlay(2L, 5L)

        assertNull(result)
    }

    @Test
    fun shouldGetPlayerNumberOverlayByHashMappedToDomain() = runTest {
        val room = RoomPlayerNumberOverlay(id = 1L, hash = "h1")
        Mockito.`when`(numberOverlayDao.getPlayerNumberOverlayByHash("h1")).thenReturn(room)

        val result = repository.getPlayerNumberOverlayByHash("h1")

        assertEquals(1L, result?.id)
    }

    @Test
    fun shouldReturnNullWhenPlayerNumberOverlayHashNotFound() = runTest {
        Mockito.`when`(numberOverlayDao.getPlayerNumberOverlayByHash("missing")).thenReturn(null)

        val result = repository.getPlayerNumberOverlayByHash("missing")

        assertNull(result)
    }
}

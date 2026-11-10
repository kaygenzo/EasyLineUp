/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.repository.dao.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.RoomPlayerFieldPosition
import com.telen.easylineup.repository.model.RoomPlayerGamesCount
import com.telen.easylineup.repository.model.RoomPlayerWithPosition
import com.telen.easylineup.repository.model.RoomPositionWithLineup
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

/**
 * Unit tests for [PlayerFieldPositionRepositoryImpl]: verifies it maps domain<->Room correctly
 * and delegates to [PlayerFieldPositionsDao] - [PlayerFieldPositionsDao] itself is mocked, so
 * this never touches a real database.
 */
@RunWith(MockitoJUnitRunner::class)
internal class PlayerFieldPositionRepositoryImplTest {
    @Mock
    lateinit var dao: PlayerFieldPositionsDao

    private lateinit var repository: PlayerFieldPositionRepositoryImpl

    @Before
    fun setup() {
        repository = PlayerFieldPositionRepositoryImpl(dao)
    }

    private fun position(id: Long = 0, lineupId: Long = 1L, playerId: Long = 1L) =
        PlayerFieldPosition(id = id, playerId = playerId, lineupId = lineupId)

    @Test
    fun shouldInsertPlayerFieldPositionsMappedToRoom() = runTest {
        repository.insertPlayerFieldPositions(
            listOf(position(playerId = 1L), position(playerId = 2L))
        )

        verify(dao).insertPlayerFieldPositions(argThat { size == 2 })
    }

    @Test
    fun shouldUpdatePlayerFieldPositionsMappedToRoom() = runTest {
        repository.updatePlayerFieldPositions(listOf(position(id = 1L)))

        verify(dao).updatePlayerFieldPositions(argThat { size == 1 && first().id == 1L })
    }

    @Test
    fun shouldUpdatePlayerFieldPositionsWithRowCountAndReturnCount() = runTest {
        Mockito.`when`(dao.updatePlayerFieldPositionsWithRowCount(any())).thenReturn(3)

        val result = repository.updatePlayerFieldPositionsWithRowCount(listOf(position(id = 1L)))

        assertEquals(3, result)
    }

    @Test
    fun shouldDeletePositionByIdWithoutMapping() = runTest {
        repository.deletePosition(position(id = 42L))

        verify(dao).deletePositionById(42L)
    }

    @Test
    fun shouldDeletePositionsMappedToRoom() = runTest {
        repository.deletePositions(listOf(position(id = 1L), position(id = 2L)))

        verify(dao).deletePositions(argThat { size == 2 })
    }

    @Test
    fun shouldUpdatePlayerFieldPositionMappedToRoom() = runTest {
        repository.updatePlayerFieldPosition(position(id = 1L))

        verify(dao).updatePlayerFieldPosition(argThat { id == 1L })
    }

    @Test
    fun shouldInsertPlayerFieldPositionMappedToRoomAndReturnId() = runTest {
        Mockito.`when`(dao.insertPlayerFieldPosition(any())).thenReturn(9L)

        val result = repository.insertPlayerFieldPosition(position(playerId = 5L))

        assertEquals(9L, result)
        verify(dao).insertPlayerFieldPosition(argThat { playerId == 5L })
    }

    @Test
    fun shouldGetPlayerFieldPositionByHashMappedToDomain() = runTest {
        val room = RoomPlayerFieldPosition(id = 1L, hash = "h1")
        Mockito.`when`(dao.getPlayerFieldPositionByHash("h1")).thenReturn(room)

        val result = repository.getPlayerFieldPositionByHash("h1")

        assertEquals(1L, result?.id)
    }

    @Test
    fun shouldReturnNullWhenPlayerFieldPositionHashNotFound() = runTest {
        Mockito.`when`(dao.getPlayerFieldPositionByHash("missing")).thenReturn(null)

        val result = repository.getPlayerFieldPositionByHash("missing")

        assertNull(result)
    }

    @Test
    fun shouldGetPlayerFieldPositionsMappedToDomain() = runTest {
        val rooms = listOf(RoomPlayerFieldPosition(id = 1L), RoomPlayerFieldPosition(id = 2L))
        Mockito.`when`(dao.getPlayerFieldPositions()).thenReturn(rooms)

        val result = repository.getPlayerFieldPositions()

        assertEquals(listOf(1L, 2L), result.map { it.id })
    }

    @Test
    fun shouldGetPlayerFieldPositionMappedToDomain() = runTest {
        val room = RoomPlayerFieldPosition(id = 1L, position = 3)
        Mockito.`when`(dao.getPlayerFieldPosition(1L)).thenReturn(room)

        val result = repository.getPlayerFieldPosition(1L)

        assertEquals(3, result.position)
    }

    @Test
    fun shouldGetAllPlayerFieldPositionsForLineupMappedToDomain() = runTest {
        val rooms = listOf(RoomPlayerFieldPosition(id = 1L, lineupId = 5L))
        Mockito.`when`(dao.getAllPlayerFieldPositionsForLineup(5L)).thenReturn(rooms)

        val result = repository.getAllPlayerFieldPositionsForLineup(5L)

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun shouldGetAllPlayersWithPositionsForLineupMappedToDomain() = runTest {
        val room = RoomPlayerWithPosition(
            playerName = "Alice",
            playerSex = 0,
            shirtNumber = 1,
            licenseNumber = "1",
            teamId = 1L,
            image = null,
            playerId = 1L,
            lineupId = 5L,
            playerPositions = 0
        )
        Mockito.`when`(dao.getAllPlayersWithPositionsForLineup(5L)).thenReturn(listOf(room))

        val result = repository.getAllPlayersWithPositionsForLineup(5L)

        assertEquals(listOf("Alice"), result.map { it.playerName })
    }

    @Test
    fun shouldGetPlayerPositionForMappedToDomain() = runTest {
        val room = RoomPlayerFieldPosition(id = 1L, lineupId = 5L, playerId = 2L)
        Mockito.`when`(dao.getPlayerPositionFor(5L, 2L)).thenReturn(room)

        val result = repository.getPlayerPositionFor(5L, 2L)

        assertEquals(1L, result?.id)
    }

    @Test
    fun shouldReturnNullWhenNoPlayerPositionFor() = runTest {
        Mockito.`when`(dao.getPlayerPositionFor(eq(5L), eq(2L))).thenReturn(null)

        val result = repository.getPlayerPositionFor(5L, 2L)

        assertNull(result)
    }

    @Test
    fun shouldGetAllPositionsForPlayerMappedToDomain() = runTest {
        val rooms = listOf(RoomPositionWithLineup(lineupName = "L", tournamentName = "T"))
        Mockito.`when`(dao.getAllPositionsForPlayer(1L)).thenReturn(rooms)

        val result = repository.getAllPositionsForPlayer(1L)

        assertEquals(listOf("L"), result.map { it.lineupName })
    }

    @Test
    fun shouldGetMostUsedPlayersMappedToDomain() = runTest {
        val rooms = listOf(RoomPlayerGamesCount(playerId = 1L, size = 5))
        Mockito.`when`(dao.getMostUsedPlayers(1L)).thenReturn(rooms)

        val result = repository.getMostUsedPlayers(1L)

        assertEquals(listOf(5), result.map { it.size })
    }
}

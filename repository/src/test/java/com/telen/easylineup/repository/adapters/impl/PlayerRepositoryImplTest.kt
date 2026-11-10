/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.repository.dao.PlayerDao
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.RoomPlayerWithPosition
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

/**
 * Unit tests for [PlayerRepositoryImpl]: verifies it maps domain<->Room correctly and delegates
 * to [PlayerDao] - [PlayerDao] itself is mocked, so this never touches a real database.
 */
@RunWith(MockitoJUnitRunner::class)
internal class PlayerRepositoryImplTest {
    @Mock
    lateinit var playerDao: PlayerDao

    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        repository = PlayerRepositoryImpl(playerDao)
    }

    private fun player(id: Long = 0, name: String = "Player", teamId: Long = 1L) =
        Player(id = id, teamId = teamId, name = name, shirtNumber = 1, licenseNumber = "1")

    @Test
    fun shouldInsertPlayerMappedToRoom() = runTest {
        Mockito.`when`(playerDao.insertPlayer(any())).thenReturn(5L)

        val result = repository.insertPlayer(player(name = "Alice"))

        assertEquals(5L, result)
        verify(playerDao).insertPlayer(argThat { name == "Alice" })
    }

    @Test
    fun shouldInsertPlayersMappedToRoom() = runTest {
        val players = listOf(player(name = "A"), player(name = "B"))

        repository.insertPlayers(players)

        verify(playerDao).insertPlayers(argThat { size == 2 })
    }

    @Test
    fun shouldDeletePlayerMappedToRoom() = runTest {
        val target = player(id = 1L)

        repository.deletePlayer(target)

        verify(playerDao).deletePlayer(argThat { id == 1L })
    }

    @Test
    fun shouldUpdatePlayerMappedToRoom() = runTest {
        val target = player(id = 1L, name = "Renamed")

        repository.updatePlayer(target)

        verify(playerDao).updatePlayer(argThat { name == "Renamed" })
    }

    @Test
    fun shouldUpdatePlayersWithRowCountAndReturnCount() = runTest {
        Mockito.`when`(playerDao.updatePlayersWithRowCount(any())).thenReturn(2)

        val result = repository.updatePlayersWithRowCount(listOf(player(id = 1L), player(id = 2L)))

        assertEquals(2, result)
    }

    @Test
    fun shouldGetPlayerByHashMappedToDomain() = runTest {
        val room = RoomPlayer(id = 1L, name = "Alice", teamId = 1L, hash = "h1")
        Mockito.`when`(playerDao.getPlayerByHash("h1")).thenReturn(room)

        val result = repository.getPlayerByHash("h1")

        assertEquals("Alice", result?.name)
    }

    @Test
    fun shouldReturnNullWhenPlayerHashNotFound() = runTest {
        Mockito.`when`(playerDao.getPlayerByHash("missing")).thenReturn(null)

        val result = repository.getPlayerByHash("missing")

        assertNull(result)
    }

    @Test
    fun shouldObservePlayerByIdMappedToDomain() = runTest {
        val room = RoomPlayer(id = 1L, name = "Alice", teamId = 1L)
        Mockito.`when`(playerDao.observePlayerById(1L)).thenReturn(flowOf(listOf(room)))

        val result = repository.observePlayerById(1L).toList()

        assertEquals("Alice", result.first().name)
    }

    @Test
    fun shouldObservePlayerByIdFallBackToEmptyPlayerWhenNoneFound() = runTest {
        Mockito.`when`(playerDao.observePlayerById(1L)).thenReturn(flowOf(emptyList()))

        val result = repository.observePlayerById(1L).toList()

        assertEquals("", result.first().name)
        assertEquals(0L, result.first().teamId)
    }

    @Test
    fun shouldGetPlayerByIdMappedToDomain() = runTest {
        val room = RoomPlayer(id = 1L, name = "Alice", teamId = 1L)
        Mockito.`when`(playerDao.getPlayerById(1L)).thenReturn(room)

        val result = repository.getPlayerById(1L)

        assertEquals("Alice", result.name)
    }

    @Test
    fun shouldGetPlayersByTeamIdMappedToDomain() = runTest {
        val rooms = listOf(
            RoomPlayer(id = 1L, name = "A", teamId = 1L),
            RoomPlayer(id = 2L, name = "B", teamId = 1L)
        )
        Mockito.`when`(playerDao.getPlayersByTeamId(1L)).thenReturn(rooms)

        val result = repository.getPlayersByTeamId(1L)

        assertEquals(listOf("A", "B"), result.map { it.name })
    }

    @Test
    fun shouldGetPlayersMappedToDomain() = runTest {
        val rooms = listOf(RoomPlayer(id = 1L, name = "A", teamId = 1L))
        Mockito.`when`(playerDao.getPlayers()).thenReturn(rooms)

        val result = repository.getPlayers()

        assertEquals(listOf("A"), result.map { it.name })
    }

    @Test
    fun shouldObservePlayersMappedToDomain() = runTest {
        val rooms = listOf(RoomPlayer(id = 1L, name = "A", teamId = 1L))
        Mockito.`when`(playerDao.observePlayersByTeamId(1L)).thenReturn(flowOf(rooms))

        val result = repository.observePlayers(1L).toList()

        assertEquals(listOf("A"), result.first().map { it.name })
    }

    @Test
    fun shouldGetTeamPlayersAndMaybePositionsMappedToDomain() = runTest {
        val room = RoomPlayerWithPosition(
            playerName = "Alice",
            playerSex = 0,
            shirtNumber = 1,
            licenseNumber = "1",
            teamId = 1L,
            image = null,
            playerId = 1L,
            lineupId = 1L,
            playerPositions = 0
        )
        Mockito.`when`(playerDao.getTeamPlayersAndMaybePositions(1L))
            .thenReturn(flowOf(listOf(room)))

        val result = repository.getTeamPlayersAndMaybePositions(1L).toList()

        assertEquals(listOf("Alice"), result.first().map { it.playerName })
    }

    @Test
    fun shouldGetShirtNumberFromPlayersMappedToDomain() = runTest {
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
        Mockito.`when`(playerDao.getShirtNumberHistoryFromPlayers(1L, 7)).thenReturn(rooms)

        val result = repository.getShirtNumberFromPlayers(1L, 7)

        assertEquals(listOf("Alice"), result.map { it.playerName })
    }
}

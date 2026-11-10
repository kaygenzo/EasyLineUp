/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.repository.dao.LineupDao
import com.telen.easylineup.repository.model.RoomLineup
import com.telen.easylineup.repository.model.RoomPlayerInLineup
import com.telen.easylineup.repository.model.RoomTournamentWithLineup
import kotlinx.coroutines.flow.first
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
 * Unit tests for [LineupRepositoryImpl]: verifies it maps domain<->Room correctly and delegates
 * to [LineupDao] - [LineupDao] itself is mocked, so this never touches a real database.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LineupRepositoryImplTest {
    @Mock
    lateinit var lineupDao: LineupDao

    private lateinit var repository: LineupRepositoryImpl

    @Before
    fun setup() {
        repository = LineupRepositoryImpl(lineupDao)
    }

    @Test
    fun shouldInsertLineupMappedToRoom() = runTest {
        val lineup = Lineup(id = 0, name = "L", teamId = 1L, tournamentId = 1L)
        Mockito.`when`(lineupDao.insertLineup(any())).thenReturn(10L)

        val result = repository.insertLineup(lineup)

        assertEquals(10L, result)
        verify(lineupDao).insertLineup(argThat { name == "L" && teamId == 1L })
    }

    @Test
    fun shouldInsertLineupsMappedToRoom() = runTest {
        val lineups = listOf(
            Lineup(id = 0, name = "A", teamId = 1L, tournamentId = 1L),
            Lineup(id = 0, name = "B", teamId = 1L, tournamentId = 1L)
        )

        repository.insertLineups(lineups)

        verify(lineupDao).insertLineups(argThat { size == 2 })
    }

    @Test
    fun shouldUpdateLineupMappedToRoom() = runTest {
        val lineup = Lineup(id = 1L, name = "Renamed", teamId = 1L, tournamentId = 1L)

        repository.updateLineup(lineup)

        verify(lineupDao).updateLineup(argThat { name == "Renamed" })
    }

    @Test
    fun shouldUpdateLineupsWithRowCountAndReturnCount() = runTest {
        val lineups = listOf(Lineup(id = 1L, name = "A", teamId = 1L, tournamentId = 1L))
        Mockito.`when`(lineupDao.updateLineupsWithRowCount(any())).thenReturn(1)

        val result = repository.updateLineupsWithRowCount(lineups)

        assertEquals(1, result)
    }

    @Test
    fun shouldDeleteLineupMappedToRoom() = runTest {
        val lineup = Lineup(id = 1L, name = "A", teamId = 1L, tournamentId = 1L)

        repository.deleteLineup(lineup)

        verify(lineupDao).deleteLineup(argThat { id == 1L })
    }

    @Test
    fun shouldDeleteLineupsMappedToRoom() = runTest {
        val lineups = listOf(
            Lineup(id = 1L, name = "A", teamId = 1L, tournamentId = 1L),
            Lineup(id = 2L, name = "B", teamId = 1L, tournamentId = 1L)
        )

        repository.deleteLineups(lineups)

        verify(lineupDao).deleteLineups(argThat { size == 2 })
    }

    @Test
    fun shouldGetLineupsMappedToDomain() = runTest {
        val rooms = listOf(RoomLineup(id = 1L, name = "A"), RoomLineup(id = 2L, name = "B"))
        Mockito.`when`(lineupDao.getLineups()).thenReturn(rooms)

        val result = repository.getLineups()

        assertEquals(listOf("A", "B"), result.map { it.name })
    }

    @Test
    fun shouldObserveLineupByIdMappedToDomain() = runTest {
        val room = RoomLineup(id = 1L, name = "A")
        Mockito.`when`(lineupDao.observeLineupById(1L)).thenReturn(flowOf(listOf(room)))

        val result = repository.observeLineupById(1L).toList()

        assertEquals("A", result.first().name)
    }

    @Test
    fun shouldObserveLineupByIdFallBackToEmptyLineupWhenNoneFound() = runTest {
        Mockito.`when`(lineupDao.observeLineupById(1L)).thenReturn(flowOf(emptyList()))

        val result = repository.observeLineupById(1L).toList()

        assertEquals(Lineup(), result.first())
    }

    @Test
    fun shouldGetLineupByHashMappedToDomain() = runTest {
        val room = RoomLineup(id = 1L, name = "A", hash = "h1")
        Mockito.`when`(lineupDao.getLineupByHash("h1")).thenReturn(room)

        val result = repository.getLineupByHash("h1")

        assertEquals("A", result?.name)
    }

    @Test
    fun shouldReturnNullWhenLineupHashNotFound() = runTest {
        Mockito.`when`(lineupDao.getLineupByHash("missing")).thenReturn(null)

        val result = repository.getLineupByHash("missing")

        assertNull(result)
    }

    @Test
    fun shouldGetLineupByIdMappedToDomain() = runTest {
        val room = RoomLineup(id = 1L, name = "A")
        Mockito.`when`(lineupDao.getLineupById(1L)).thenReturn(room)

        val result = repository.getLineupById(1L)

        assertEquals("A", result.name)
    }

    @Test
    fun shouldGetLineupsForTournamentMappedToDomain() = runTest {
        val rooms = listOf(RoomLineup(id = 1L, name = "A"))
        Mockito.`when`(lineupDao.getLineupsForTournament(5L, 1L)).thenReturn(rooms)

        val result = repository.getLineupsForTournament(5L, 1L)

        assertEquals(listOf("A"), result.map { it.name })
    }

    @Test
    fun shouldGetLastLineupMappedToDomain() = runTest {
        val room = RoomLineup(id = 1L, name = "Last")
        Mockito.`when`(lineupDao.getLastLineup(1L)).thenReturn(room)

        val result = repository.getLastLineup(1L)

        assertEquals("Last", result?.name)
    }

    @Test
    fun shouldReturnNullWhenNoLastLineup() = runTest {
        Mockito.`when`(lineupDao.getLastLineup(1L)).thenReturn(null)

        val result = repository.getLastLineup(1L)

        assertNull(result)
    }

    @Test
    fun shouldObserveAllTournamentsWithLineupsMappedToDomain() = runTest {
        val rooms = listOf(RoomTournamentWithLineup(tournamentId = 1L, tournamentName = "Cup"))
        Mockito.`when`(lineupDao.observeAllTournamentsWithLineups("filter", 1L))
            .thenReturn(flowOf(rooms))

        val result = repository.observeAllTournamentsWithLineups("filter", 1L).first()

        assertEquals(listOf("Cup"), result.map { it.tournamentName })
    }

    @Test
    fun shouldGetAllPlayerPositionsForTournamentMappedToDomain() = runTest {
        val rooms = listOf(
            RoomPlayerInLineup(lineupName = "L", playerName = "Player", position = 1, playerId = 1L)
        )
        Mockito.`when`(lineupDao.getAllPlayerPositionsForTournament(5L, 1L)).thenReturn(rooms)

        val result = repository.getAllPlayerPositionsForTournament(5L, 1L)

        assertEquals(listOf("Player"), result.map { it.playerName })
    }
}

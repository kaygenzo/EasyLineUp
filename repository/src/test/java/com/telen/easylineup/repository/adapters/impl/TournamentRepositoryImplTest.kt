/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.repository.dao.TournamentDao
import com.telen.easylineup.repository.model.RoomTournament
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
 * Unit tests for [TournamentRepositoryImpl]: verifies it maps domain<->Room correctly and
 * delegates to [TournamentDao] - [TournamentDao] itself is mocked, so this never touches a real
 * database.
 */
@RunWith(MockitoJUnitRunner::class)
internal class TournamentRepositoryImplTest {
    @Mock
    lateinit var tournamentDao: TournamentDao

    private lateinit var repository: TournamentRepositoryImpl

    @Before
    fun setup() {
        repository = TournamentRepositoryImpl(tournamentDao)
    }

    private fun tournament(id: Long = 0, name: String = "Champs") =
        Tournament(id = id, name = name, createdAt = 1L, startTime = 1L, endTime = 2L)

    @Test
    fun shouldGetTournamentsMappedToDomain() = runTest {
        val rooms = listOf(RoomTournament(id = 1L, name = "A"), RoomTournament(id = 2L, name = "B"))
        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(rooms)

        val result = repository.getTournaments()

        assertEquals(listOf("A", "B"), result.map { it.name })
    }

    @Test
    fun shouldObserveTournamentsMappedToDomain() = runTest {
        val rooms = listOf(RoomTournament(id = 1L, name = "A"))
        Mockito.`when`(tournamentDao.observeTournaments()).thenReturn(flowOf(rooms))

        val result = repository.observeTournaments().toList()

        assertEquals(listOf("A"), result.first().map { it.name })
    }

    @Test
    fun shouldGetTournamentByHashMappedToDomain() = runTest {
        val room = RoomTournament(id = 1L, name = "Champs", hash = "h1")
        Mockito.`when`(tournamentDao.getTournamentByHash("h1")).thenReturn(room)

        val result = repository.getTournamentByHash("h1")

        assertEquals("Champs", result?.name)
    }

    @Test
    fun shouldReturnNullWhenTournamentHashNotFound() = runTest {
        Mockito.`when`(tournamentDao.getTournamentByHash("missing")).thenReturn(null)

        val result = repository.getTournamentByHash("missing")

        assertNull(result)
    }

    @Test
    fun shouldGetTournamentByNameMappedToDomain() = runTest {
        val room = RoomTournament(id = 1L, name = "Champs")
        Mockito.`when`(tournamentDao.getTournamentByName("Champs")).thenReturn(room)

        val result = repository.getTournamentByName("Champs")

        assertEquals(1L, result?.id)
    }

    @Test
    fun shouldInsertTournamentMappedToRoom() = runTest {
        Mockito.`when`(tournamentDao.insertTournament(any())).thenReturn(7L)

        val result = repository.insertTournament(tournament(name = "Champs"))

        assertEquals(7L, result)
        verify(tournamentDao).insertTournament(argThat { name == "Champs" })
    }

    @Test
    fun shouldUpdateTournamentMappedToRoom() = runTest {
        repository.updateTournament(tournament(id = 1L, name = "Renamed"))

        verify(tournamentDao).updateTournament(argThat { name == "Renamed" })
    }

    @Test
    fun shouldUpdateTournamentsWithRowCountAndReturnCount() = runTest {
        Mockito.`when`(tournamentDao.updateTournamentsWithRowCount(any())).thenReturn(1)

        val result = repository.updateTournamentsWithRowCount(listOf(tournament(id = 1L)))

        assertEquals(1, result)
    }

    @Test
    fun shouldDeleteTournamentMappedToRoom() = runTest {
        repository.deleteTournament(tournament(id = 1L))

        verify(tournamentDao).deleteTournament(argThat { id == 1L })
    }

    @Test
    fun shouldDeleteTournamentsMappedToRoom() = runTest {
        repository.deleteTournaments(listOf(tournament(id = 1L), tournament(id = 2L)))

        verify(tournamentDao).deleteTournaments(argThat { size == 2 })
    }
}

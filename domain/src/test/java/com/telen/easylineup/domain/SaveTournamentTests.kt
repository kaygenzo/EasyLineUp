/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
import com.telen.easylineup.domain.usecases.exceptions.TournamentEndBeforeStartException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
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
internal class SaveTournamentTests {
    @Mock lateinit var repository: TournamentRepository
    lateinit var saveTournament: SaveTournament
    lateinit var tournament: Tournament

    @Before
    fun init() {
        saveTournament = SaveTournament(repository, fixedClock(), testDispatcherProvider())
        tournament = Tournament(
            id = 0L,
            name = "champs",
            createdAt = 0L,
            startTime = 0L,
            endTime = 0L
        )
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() = runTest {
        tournament = tournament.copy(name = "")

        val result = saveTournament(tournament)

        assertTrue(result.exceptionOrNull() is TournamentNameEmptyException)
        verify(repository, never()).getTournaments()
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsOnlySpaces() = runTest {
        tournament = tournament.copy(name = "   ")

        val result = saveTournament(tournament)

        assertTrue(result.exceptionOrNull() is TournamentNameEmptyException)
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldTrimTheNameBeforeCheckingAndSavingIt() = runTest {
        Mockito.`when`(repository.getTournaments()).thenReturn(emptyList())
        Mockito.`when`(repository.insertTournament(any())).thenReturn(42L)

        val result = saveTournament(tournament.copy(name = "  champs "))

        assertEquals("champs", result.getOrNull()?.name)
        verify(repository).insertTournament(argThat { name == "champs" })
    }

    @Test
    fun shouldRefuseATournamentEndingBeforeItStarts() = runTest {
        tournament = tournament.copy(startTime = 2_000L, endTime = 1_000L)

        val result = saveTournament(tournament)

        assertTrue(result.exceptionOrNull() is TournamentEndBeforeStartException)
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldAcceptAOneDayTournament() = runTest {
        Mockito.`when`(repository.getTournaments()).thenReturn(emptyList())
        Mockito.`when`(repository.insertTournament(any())).thenReturn(42L)

        val result = saveTournament(tournament.copy(startTime = 1_000L, endTime = 1_000L))

        assertTrue(result.isSuccess)
    }

    @Test
    fun shouldInsertTournamentWhenNameDoesNotExist() = runTest {
        Mockito.`when`(repository.getTournaments()).thenReturn(emptyList())
        Mockito.`when`(repository.insertTournament(any())).thenReturn(42L)

        val result = saveTournament(tournament)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull()?.id)
        assertEquals(TEST_NOW, result.getOrNull()?.createdAt)
        verify(repository).insertTournament(any())
    }

    @Test
    fun shouldTriggerAlreadyExistingExceptionAndNotInsertWhenNameAlreadyExists() = runTest {
        val existing = tournament.copy(id = 1L)
        Mockito.`when`(repository.getTournaments()).thenReturn(listOf(existing))

        val result = saveTournament(tournament)

        assertTrue(result.exceptionOrNull() is AlreadyExistingTournamentException)
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldRefuseANameAlreadyTakenWithAnotherCase() = runTest {
        val existing = tournament.copy(id = 1L, name = "Champs")
        Mockito.`when`(repository.getTournaments()).thenReturn(listOf(existing))

        val result = saveTournament(tournament.copy(name = "CHAMPS"))

        assertTrue(result.exceptionOrNull() is AlreadyExistingTournamentException)
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldPropagateUnrelatedInsertErrors() = runTest {
        val exception = Exception("db error")
        Mockito.`when`(repository.getTournaments()).thenReturn(emptyList())
        Mockito.`when`(repository.insertTournament(any())).thenAnswer { throw exception }

        val result = saveTournament(tournament)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == exception.message)
    }

    /**
     * [repository.getTournaments] failing for an actual repository error must propagate,
     * not be treated as "no tournament with this name yet".
     */
    @Test
    fun shouldPropagateLookupErrorsInsteadOfTreatingThemAsNotFound() = runTest {
        val exception = RuntimeException("connection lost")
        Mockito.`when`(repository.getTournaments()).thenAnswer { throw exception }

        val result = saveTournament(tournament)

        assertTrue(result.exceptionOrNull() === exception)
        verify(repository, never()).insertTournament(any())
    }
}

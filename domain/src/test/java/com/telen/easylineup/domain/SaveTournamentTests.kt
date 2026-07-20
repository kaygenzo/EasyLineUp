/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SaveTournamentTests {
    @Mock lateinit var repository: TournamentRepository
    lateinit var saveTournament: SaveTournament
    lateinit var tournament: Tournament

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        saveTournament = SaveTournament(repository, testDispatcherProvider())
        tournament = Tournament(id = 0L, name = "champs", createdAt = 0L, startTime = 0L, endTime = 0L)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() = runTest {
        tournament.name = ""

        val result = saveTournament(tournament)

        assertTrue(result.exceptionOrNull() is TournamentNameEmptyException)
        verify(repository, never()).getTournamentByName(any())
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldInsertTournamentWhenNameDoesNotExist() = runTest {
        Mockito.`when`(repository.getTournamentByName("champs"))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(repository.insertTournament(any())).thenReturn(42L)

        val result = saveTournament(tournament)

        assertTrue(result.isSuccess)
        verify(repository).insertTournament(any())
    }

    @Test
    fun shouldTriggerAlreadyExistingExceptionAndNotInsertWhenNameAlreadyExists() = runTest {
        val existing = tournament.copy(id = 1L)
        Mockito.`when`(repository.getTournamentByName("champs")).thenReturn(existing)

        val result = saveTournament(tournament)

        assertTrue(result.exceptionOrNull() is AlreadyExistingTournamentException)
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldPropagateUnrelatedInsertErrors() = runTest {
        val exception = Exception("db error")
        Mockito.`when`(repository.getTournamentByName("champs"))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(repository.insertTournament(any())).thenAnswer { throw exception }

        val result = saveTournament(tournament)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == exception.message)
    }
}

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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SaveTournamentTests {
    val observer: TestObserver<Void> = TestObserver()
    @Mock lateinit var repository: TournamentRepository
    lateinit var saveTournament: SaveTournament
    lateinit var tournament: Tournament

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        saveTournament = SaveTournament(repository, testSchedulersProvider())
        tournament = Tournament(id = 0L, name = "champs", createdAt = 0L, startTime = 0L, endTime = 0L)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() {
        tournament.name = ""

        saveTournament(tournament).subscribe(observer)
        observer.await()

        observer.assertError(TournamentNameEmptyException::class.java)
        verify(repository, never()).getTournamentByName(any())
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldInsertTournamentWhenNameDoesNotExist() {
        Mockito.`when`(repository.getTournamentByName("champs"))
            .thenReturn(Single.error(NoSuchElementException()))
        Mockito.`when`(repository.insertTournament(any())).thenReturn(Single.just(42L))

        saveTournament(tournament).subscribe(observer)
        observer.await()

        observer.assertComplete()
        verify(repository).insertTournament(any())
    }

    @Test
    fun shouldTriggerAlreadyExistingExceptionAndNotInsertWhenNameAlreadyExists() {
        val existing = tournament.copy(id = 1L)
        Mockito.`when`(repository.getTournamentByName("champs")).thenReturn(Single.just(existing))

        saveTournament(tournament).subscribe(observer)
        observer.await()

        observer.assertError(AlreadyExistingTournamentException::class.java)
        verify(repository, never()).insertTournament(any())
    }

    @Test
    fun shouldPropagateUnrelatedInsertErrors() {
        val exception = Exception("db error")
        Mockito.`when`(repository.getTournamentByName("champs"))
            .thenReturn(Single.error(NoSuchElementException()))
        Mockito.`when`(repository.insertTournament(any())).thenReturn(Single.error(exception))

        saveTournament(tournament).subscribe(observer)
        observer.await()

        observer.assertError(exception)
    }
}

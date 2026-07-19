/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.UpdateLineup
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class UpdateLineupTests {
    val observer: TestObserver<Void> = TestObserver()
    @Mock lateinit var lineupRepo: LineupRepository
    lateinit var updateLineup: UpdateLineup
    lateinit var lineup: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        updateLineup = UpdateLineup(lineupRepo, testSchedulersProvider())
        lineup = Lineup(id = 1L, name = "test", teamId = 1L, tournamentId = 1L)
    }

    @Test
    fun shouldUpdateLineupThroughRepository() {
        Mockito.`when`(lineupRepo.updateLineup(lineup)).thenReturn(Completable.complete())

        updateLineup(lineup).subscribe(observer)
        observer.await()

        observer.assertComplete()
        verify(lineupRepo).updateLineup(lineup)
    }

    @Test
    fun shouldPropagateErrorFromRepository() {
        val exception = Exception("db error")
        Mockito.`when`(lineupRepo.updateLineup(lineup)).thenReturn(Completable.error(exception))

        updateLineup(lineup).subscribe(observer)
        observer.await()

        observer.assertError(exception)
    }
}

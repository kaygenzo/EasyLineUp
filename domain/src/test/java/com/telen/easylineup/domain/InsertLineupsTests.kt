/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.InsertLineups
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
internal class InsertLineupsTests {
    @Mock lateinit var lineupDao: LineupRepository
    lateinit var insertLineups: InsertLineups

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertLineups = InsertLineups(lineupDao, testSchedulersProvider())
    }

    @Test
    fun shouldDelegateToRepository() {
        val lineups = listOf(Lineup(id = 1L, name = "toto", teamId = 1L, tournamentId = 1L))
        Mockito.`when`(lineupDao.insertLineups(lineups)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        insertLineups(lineups).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(lineupDao).insertLineups(lineups)
    }
}

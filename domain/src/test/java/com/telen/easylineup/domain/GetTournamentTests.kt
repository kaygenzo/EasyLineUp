/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.GetTournaments
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTournamentTests {
    val observer: TestObserver<GetTournaments.ResponseValue> = TestObserver()
    @Mock lateinit var tournamentDao: TournamentRepository
    lateinit var getTournaments: GetTournaments
    lateinit var tournaments: MutableList<Tournament>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getTournaments = GetTournaments(tournamentDao)

        tournaments = mutableListOf()
        tournaments.add(Tournament(1, "toto", 1L, 2L, 3L, null))
        tournaments.add(Tournament(2, "tata", 2L, 3L, 4L, null))
        tournaments.add(Tournament(3, "titi", 3L, 4L, 5L, null))

        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(Single.just(tournaments))
    }

    @Test
    fun shouldGetTournaments() {
        getTournaments.executeUseCase(GetTournaments.RequestValues()).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(tournaments, observer.values().first().tournaments)
    }
}

package com.telen.easylineup.domain

import com.telen.easylineup.domain.usecases.GetTournaments
import com.telen.easylineup.repository.dao.TournamentDao
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class GetTournamentTests {

    @Mock lateinit var tournamentDao: TournamentDao
    lateinit var mGetTournaments: GetTournaments
    lateinit var tournaments: MutableList<Tournament>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetTournaments = GetTournaments(tournamentDao)

        tournaments = mutableListOf()
        tournaments.add(Tournament(1, "toto", 1L))
        tournaments.add(Tournament(2, "tata", 2L))
        tournaments.add(Tournament(3, "titi", 3L))

        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(Single.just(tournaments))
    }

    @Test
    fun shouldGetTournaments() {
        val observer = TestObserver<GetTournaments.ResponseValue>()
        mGetTournaments.executeUseCase(GetTournaments.RequestValues()).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(tournaments, observer.values().first().tournaments)
    }
}
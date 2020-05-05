package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.Completable
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
internal class CreateLineupTests {

    @Mock lateinit var tournamentDao: TournamentRepository
    @Mock lateinit var lineupDao: LineupRepository
    lateinit var mCreateLineup: CreateLineup
    lateinit var roster: MutableList<RosterPlayerStatus>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mCreateLineup = CreateLineup(tournamentDao, lineupDao)

        roster = mutableListOf()
        roster.add(RosterPlayerStatus(Player(1,1,"toto",1,1), true))
        roster.add(RosterPlayerStatus(Player(2,1,"tata",1,1), true))
        roster.add(RosterPlayerStatus(Player(3,1,"titi",1,1), true))

        Mockito.`when`(tournamentDao.insertTournament(any())).thenReturn(Single.just(1L))
        Mockito.`when`(tournamentDao.updateTournament(any())).thenReturn(Completable.complete())
        Mockito.`when`(lineupDao.insertLineup(any())).thenReturn(Single.just(1L))
    }

    @Test
    fun shouldInsertTournamentIfNotExists() {
        val tournament = Tournament(0L, "tata", 1L)
        val observer = TestObserver<CreateLineup.ResponseValue>()
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, tournament, "title", mutableListOf())).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(tournamentDao).insertTournament(tournament)
        verify(tournamentDao, never()).updateTournament(tournament)
    }

    @Test
    fun shouldUpdateTournamentIfAlreadyExists() {
        val tournament = Tournament(1L, "tata", 1L)
        val observer = TestObserver<CreateLineup.ResponseValue>()
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, tournament, "title", mutableListOf())).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(tournamentDao).updateTournament(tournament)
        verify(tournamentDao, never()).insertTournament(tournament)
    }

    @Test
    fun shouldLineupSavedWithRosterNullForAll() {
        val tournament = Tournament(1L, "tata", 1L)
        val observer = TestObserver<CreateLineup.ResponseValue>()
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, tournament, "title", roster)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        val expectedLineup = Lineup(id = 0, name = "title", tournamentId = 1L, teamId = 1L, roster = null)
        verify(lineupDao).insertLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(expectedLineup, it)
        })
    }

    @Test
    fun shouldLineupSavedWithRosterNotNullForSelection() {
        val tournament = Tournament(1L, "tata", 1L)
        roster.add(RosterPlayerStatus(Player(4,1,"tutu",1,1), false))
        val observer = TestObserver<CreateLineup.ResponseValue>()
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, tournament, "title", roster)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        val expectedLineup = Lineup(id = 0, name = "title", tournamentId = 1L, teamId = 1L, roster = "1;2;3")
        verify(lineupDao).insertLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(expectedLineup, it)
        })
    }

    @Test
    fun shouldSavedSuccessfullyTheNewLineup() {
        val tournament = Tournament(1L, "tata", 1L)
        val observer = TestObserver<CreateLineup.ResponseValue>()
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, tournament, "title", roster)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(1L, observer.values().first().lineupID)
    }

    @Test
    fun shouldTriggerAnExceptionIfLineupNameEmpty() {
        val tournament = Tournament(1L, "Tournament", 1L)
        val observer = TestObserver<CreateLineup.ResponseValue>()
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, tournament, "      ", roster)).subscribe(observer)
        observer.await()
        observer.assertError(LineupNameEmptyException::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfTournamentNameEmpty() {
        val tournament = Tournament(1L, "    ", 1L)
        val observer = TestObserver<CreateLineup.ResponseValue>()
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, tournament, "title", roster)).subscribe(observer)
        observer.await()
        observer.assertError(TournamentNameEmptyException::class.java)
    }
}
package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteAllData
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
internal class DeleteAllDataTests {

    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    lateinit var mDeleteAllData: DeleteAllData

    private val tournaments = mutableListOf<Tournament>()
    private val teams = mutableListOf<Team>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mDeleteAllData = DeleteAllData(teamDao, tournamentDao)

        tournaments.add(Tournament(1, "t1", 1L, 2L, 3L, null))
        tournaments.add(Tournament(2, "t2", 2L, 3L, 4L, null))
        tournaments.add(Tournament(3, "t3", 3L, 4L, 5L, null))

        teams.add(Team(1, "t1", null, TeamType.BASEBALL.id, true))
        teams.add(Team(2, "t2", null, TeamType.SOFTBALL.id, false))

        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(Single.just(tournaments))
        Mockito.`when`(tournamentDao.deleteTournaments(tournaments)).thenReturn(Completable.complete())
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
        Mockito.`when`(teamDao.deleteTeams(teams)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotGetTournaments() {
        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(Single.error(Exception()))
        val observer = TestObserver<DeleteAllData.ResponseValue>()
        mDeleteAllData.executeUseCase(DeleteAllData.RequestValues()).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotDeleteTournaments() {
        Mockito.`when`(tournamentDao.deleteTournaments(tournaments)).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<DeleteAllData.ResponseValue>()
        mDeleteAllData.executeUseCase(DeleteAllData.RequestValues()).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotGetTeams() {
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.error(Exception()))
        val observer = TestObserver<DeleteAllData.ResponseValue>()
        mDeleteAllData.executeUseCase(DeleteAllData.RequestValues()).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfCannotDeleteTeams() {
        Mockito.`when`(teamDao.deleteTeams(teams)).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<DeleteAllData.ResponseValue>()
        mDeleteAllData.executeUseCase(DeleteAllData.RequestValues()).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldSuccessfullyDeleteAllData() {
        val observer = TestObserver<DeleteAllData.ResponseValue>()
        mDeleteAllData.executeUseCase(DeleteAllData.RequestValues()).subscribe(observer)
        observer.await()
        observer.assertComplete()
    }
}
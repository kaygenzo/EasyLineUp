package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.data.TournamentDao
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.TeamType
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DeleteAllDataTests {

    @Mock lateinit var teamDao: TeamDao
    @Mock lateinit var tournamentDao: TournamentDao
    lateinit var mDeleteAllData: DeleteAllData

    private val tournaments = mutableListOf<Tournament>()
    private val teams = mutableListOf<Team>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mDeleteAllData = DeleteAllData(teamDao, tournamentDao)

        tournaments.add(Tournament(1, "t1", 1L))
        tournaments.add(Tournament(2, "t2", 2L))
        tournaments.add(Tournament(3, "t3", 3L))

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
package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.data.TournamentDao
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.Tournament
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
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class GetAllTeamsTests {

    @Mock lateinit var teamDao: TeamDao
    lateinit var mGetAllTeams: GetAllTeams
    lateinit var teams: MutableList<Team>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetAllTeams = GetAllTeams(teamDao)

        teams = mutableListOf()
        teams.add(Team(1, "toto", null, 0, true))
        teams.add(Team(2, "tata", null, 0, true))
        teams.add(Team(3, "titi", null, 0, true))
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
    }

    @Test
    fun shouldDeleteTournament() {
        val observer = TestObserver<GetAllTeams.ResponseValue>()
        mGetAllTeams.executeUseCase(GetAllTeams.RequestValues())
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(teams, observer.values().first().teams)
    }
}
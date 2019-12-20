package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Team
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
class GetTeamTests {

    @Mock lateinit var teamDao: TeamDao
    lateinit var mGetTeam: GetTeam
    private var teams = mutableListOf<Team>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetTeam = GetTeam(teamDao)

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
    }

    @Test
    fun shouldGetTheFirstTeam() {

        teams.add(Team(1, "toto", null, 0, true))
        teams.add(Team(2, "tata", null, 0, false))
        teams.add(Team(3, "titi", null, 0, false))

        val observer = TestObserver<GetTeam.ResponseValue>()
        mGetTeam.executeUseCase(GetTeam.RequestValues())
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(teams[0], observer.values().first().team)
    }

    @Test
    fun shouldGetTheLastTeam() {

        teams.add(Team(1, "toto", null, 0, false))
        teams.add(Team(2, "tata", null, 0, false))
        teams.add(Team(3, "titi", null, 0, true))

        val observer = TestObserver<GetTeam.ResponseValue>()
        mGetTeam.executeUseCase(GetTeam.RequestValues())
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(teams[2], observer.values().first().team)
    }
}
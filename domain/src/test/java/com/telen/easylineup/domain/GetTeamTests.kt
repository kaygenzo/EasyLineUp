package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
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
internal class GetTeamTests {

    @Mock lateinit var teamDao: TeamRepository
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
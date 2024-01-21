/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

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
    private var teams: MutableList<Team> = mutableListOf()
    val observer: TestObserver<GetTeam.ResponseValue> = TestObserver()
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getTeam: GetTeam

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getTeam = GetTeam(teamDao)

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
    }

    @Test
    fun shouldGetTheFirstTeam() {
        teams.add(Team(1, "toto", null, 0, true))
        teams.add(Team(2, "tata", null, 0, false))
        teams.add(Team(3, "titi", null, 0, false))

        getTeam.executeUseCase(GetTeam.RequestValues())
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

        getTeam.executeUseCase(GetTeam.RequestValues())
            .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(teams[2], observer.values().first().team)
    }
}

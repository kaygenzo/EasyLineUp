/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import io.reactivex.rxjava3.core.Completable
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
internal class SaveCurrentTeamTests {
    private var teams: MutableList<Team> = mutableListOf()
    val observer: TestObserver<SaveCurrentTeam.ResponseValue> = TestObserver()
    @Mock lateinit var teamDao: TeamRepository
    lateinit var saveCurrentTeam: SaveCurrentTeam
    lateinit var newTeam: Team

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        saveCurrentTeam = SaveCurrentTeam(teamDao)

        newTeam = Team(1, "toto", null, 0, true)
        teams.add(newTeam)
        teams.add(Team(2, "tata", null, 0, true))
        teams.add(Team(3, "titi", null, 0, true))

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
        Mockito.`when`(teamDao.updateTeams(teams)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldChangeOfMainTeam() {
        saveCurrentTeam.executeUseCase(SaveCurrentTeam.RequestValues(newTeam)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).updateTeams(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(true, it[0].main)
            Assert.assertEquals(false, it[1].main)
            Assert.assertEquals(false, it[2].main)
        })
    }
}

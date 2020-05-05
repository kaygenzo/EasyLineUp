package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import com.telen.easylineup.repository.dao.TeamDao
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
class SaveCurrentTeamTests {

    @Mock lateinit var teamDao: TeamDao
    lateinit var mSaveCurrentTeam: SaveCurrentTeam
    lateinit var newTeam: Team
    private var teams = mutableListOf<Team>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSaveCurrentTeam = SaveCurrentTeam(teamDao)

        newTeam = Team(1, "toto", null, 0, true)
        teams.add(newTeam)
        teams.add(Team(2, "tata", null, 0, true))
        teams.add(Team(3, "titi", null, 0, true))

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
        Mockito.`when`(teamDao.updateTeams(teams)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldChangeOfMainTeam() {
        val observer = TestObserver<SaveCurrentTeam.ResponseValue>()
        mSaveCurrentTeam.executeUseCase(SaveCurrentTeam.RequestValues(newTeam)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).updateTeams(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(true, it[0].main)
            Assert.assertEquals(false, it[1].main)
            Assert.assertEquals(false, it[2].main)
        })
    }
}
package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.TeamType
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
class DeleteTeamTests {

    @Mock lateinit var teamDao: TeamDao
    lateinit var mDeleteTeam: DeleteTeam
    lateinit var mTeam: Team
    lateinit var mTeam2: Team
    lateinit var mTeam3: Team
    lateinit var teams: List<Team>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mDeleteTeam = DeleteTeam(teamDao)
        mTeam = Team(id = 1L, name = "toto", type = TeamType.BASEBALL.id, main = true)
        mTeam2 = Team(id = 2L, name = "tata", type = TeamType.SOFTBALL.id, main = false)
        mTeam3 = Team(id = 3L, name = "titi", type = TeamType.SOFTBALL.id, main = false)
        teams = mutableListOf(mTeam2, mTeam3)
        Mockito.`when`(teamDao.deleteTeam(mTeam)).thenReturn(Completable.complete())
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
        Mockito.`when`(teamDao.updateTeam(mTeam2)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnExceptionIfItWasTheOnlyTeamInDatabase() {
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(mutableListOf()))
        val observer = TestObserver<DeleteTeam.ResponseValue>()
        mDeleteTeam.executeUseCase(DeleteTeam.RequestValues(mTeam))
                .subscribe(observer)
        observer.await()
        observer.assertError(NoSuchElementException::class.java)
    }

    @Test
    fun shouldReassignMainTeamToTheFirstElement() {
        val observer = TestObserver<DeleteTeam.ResponseValue>()
        mDeleteTeam.executeUseCase(DeleteTeam.RequestValues(mTeam))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).updateTeam(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(2L, it.id)
            Assert.assertEquals(true, it.main)
        })
        verify(teamDao, times(1)).updateTeam(any())
    }

    @Test
    fun shouldNotReassignMainTeam() {
        mTeam.main = false
        mTeam3.main = true
        val observer = TestObserver<DeleteTeam.ResponseValue>()
        mDeleteTeam.executeUseCase(DeleteTeam.RequestValues(mTeam))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao, never()).updateTeam(any())
    }

    @Test
    fun shouldDeleteSuccessfully() {
        val observer = TestObserver<DeleteTeam.ResponseValue>()
        mDeleteTeam.executeUseCase(DeleteTeam.RequestValues(mTeam))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
    }
}
package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
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
class SaveTeamTests {

    @Mock lateinit var teamDao: TeamDao
    lateinit var mSaveTeam: SaveTeam
    lateinit var mTeam: Team

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSaveTeam = SaveTeam(teamDao)
        mTeam = Team(id = 1L, name = "test", type = TeamType.BASEBALL.id, main = true)
        Mockito.`when`(teamDao.insertTeam(any())).thenReturn(Single.just(2L))
        Mockito.`when`(teamDao.updateTeam(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() {
        mTeam.name = ""
        val request = SaveTeam.RequestValues(mTeam)
        val observer = TestObserver<SaveTeam.ResponseValue>()
        mSaveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsOnlyWhitespaces() {
        mTeam.name = "\n\t\r       "
        val request = SaveTeam.RequestValues(mTeam)
        val observer = TestObserver<SaveTeam.ResponseValue>()
        mSaveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldUpdateTeamIfIdGreaterThatZero() {
        val request = SaveTeam.RequestValues(mTeam)
        val observer = TestObserver<SaveTeam.ResponseValue>()
        mSaveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).updateTeam(any())
        verify(teamDao, never()).insertTeam(any())
        Assert.assertEquals(1L, observer.values().first().team.id)
    }

    @Test
    fun shouldInsertTeamIfIdEqualsToZero() {
        mTeam.id = 0L
        val request = SaveTeam.RequestValues(mTeam)
        val observer = TestObserver<SaveTeam.ResponseValue>()
        mSaveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).insertTeam(any())
        verify(teamDao, never()).updateTeam(any())
        Assert.assertEquals(2L, observer.values().first().team.id)
    }

    @Test
    fun shouldCorrectTeamTypeIfUnknown() {
        mTeam.type = TeamType.UNKNOWN.id
        val request = SaveTeam.RequestValues(mTeam)
        val observer = TestObserver<SaveTeam.ResponseValue>()
        mSaveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(TeamType.BASEBALL.id, observer.values().first().team.type)
    }
}
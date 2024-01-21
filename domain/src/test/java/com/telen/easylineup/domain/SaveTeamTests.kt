/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.SaveTeam
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
internal class SaveTeamTests {
    val observer: TestObserver<SaveTeam.ResponseValue> = TestObserver()
    @Mock lateinit var teamDao: TeamRepository
    lateinit var saveTeam: SaveTeam
    lateinit var team: Team

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        saveTeam = SaveTeam(teamDao)
        team = Team(id = 1L, name = "test", type = TeamType.BASEBALL.id, main = true)
        Mockito.`when`(teamDao.insertTeam(any())).thenReturn(Single.just(2L))
        Mockito.`when`(teamDao.updateTeam(any())).thenReturn(Completable.complete())
    }

    // @Test
    // fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() {
    // mTeam.name = ""
    // val request = SaveTeam.RequestValues(mTeam)
    // val observer = TestObserver<SaveTeam.ResponseValue>()
    // saveTeam.executeUseCase(request).subscribe(observer)
    // observer.await()
    // observer.assertError(NameEmptyException::class.java)
    // }

    // @Test
    // fun shouldTriggerNameEmptyExceptionIfNameIsOnlyWhitespaces() {
    // mTeam.name = "\n\t\r       "
    // val request = SaveTeam.RequestValues(mTeam)
    // val observer = TestObserver<SaveTeam.ResponseValue>()
    // saveTeam.executeUseCase(request).subscribe(observer)
    // observer.await()
    // observer.assertError(NameEmptyException::class.java)
    // }

    @Test
    fun shouldUpdateTeamIfIdGreaterThatZero() {
        val request = SaveTeam.RequestValues(team)
        saveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).updateTeam(any())
        verify(teamDao, never()).insertTeam(any())
        Assert.assertEquals(1L, observer.values().first().team.id)
    }

    @Test
    fun shouldInsertTeamIfIdEqualsToZero() {
        team.id = 0L
        val request = SaveTeam.RequestValues(team)
        saveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).insertTeam(any())
        verify(teamDao, never()).updateTeam(any())
        Assert.assertEquals(2L, observer.values().first().team.id)
    }

    @Test
    fun shouldCorrectTeamTypeIfUnknown() {
        team.type = TeamType.UNKNOWN.id
        val request = SaveTeam.RequestValues(team)
        saveTeam.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(TeamType.BASEBALL.id, observer.values().first().team.type)
    }
}

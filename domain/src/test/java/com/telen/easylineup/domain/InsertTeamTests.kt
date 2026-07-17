/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.InsertTeam
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class InsertTeamTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var insertTeam: InsertTeam

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertTeam = InsertTeam(teamDao)
    }

    @Test
    fun shouldDelegateToRepository() {
        val team = Team(id = 0L, name = "Panthers")
        Mockito.`when`(teamDao.insertTeam(team)).thenReturn(Single.just(5L))

        val observer = TestObserver<InsertTeam.ResponseValue>()
        insertTeam.executeUseCase(InsertTeam.RequestValues(team)).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(5L, observer.values().first().id)
    }
}

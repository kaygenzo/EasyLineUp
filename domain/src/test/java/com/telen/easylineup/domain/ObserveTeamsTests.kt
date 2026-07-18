/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.ObserveTeams
import io.reactivex.rxjava3.core.Flowable
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObserveTeamsTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var observeTeams: ObserveTeams

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observeTeams = ObserveTeams(teamDao)
    }

    @Test
    fun shouldDelegateToRepository() {
        val flowable = Flowable.just(listOf<Team>())
        Mockito.`when`(teamDao.getTeams()).thenReturn(flowable)

        assertSame(flowable, observeTeams())
    }
}

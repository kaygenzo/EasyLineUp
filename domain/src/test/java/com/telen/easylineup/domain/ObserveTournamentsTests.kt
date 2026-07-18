/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import androidx.lifecycle.MutableLiveData
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.ObserveTournaments
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ObserveTournamentsTests {
    @Mock lateinit var tournamentDao: TournamentRepository
    lateinit var observeTournaments: ObserveTournaments

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observeTournaments = ObserveTournaments(tournamentDao)
    }

    @Test
    fun shouldDelegateToRepository() {
        val liveData = MutableLiveData<List<Tournament>>()
        Mockito.`when`(tournamentDao.observeTournaments()).thenReturn(liveData)

        assertSame(liveData, observeTournaments())
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.ObserveTournaments
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun shouldDelegateToRepository() = runTest {
        val tournaments = listOf<Tournament>()
        Mockito.`when`(tournamentDao.observeTournaments()).thenReturn(flowOf(tournaments))

        val result = observeTournaments().toList()

        assertEquals(listOf(tournaments), result)
    }
}

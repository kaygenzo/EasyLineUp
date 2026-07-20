/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.InsertTournaments
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class InsertTournamentsTests {
    @Mock lateinit var tournamentDao: TournamentRepository
    lateinit var insertTournaments: InsertTournaments

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertTournaments = InsertTournaments(tournamentDao, testDispatcherProvider())
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val tournaments = listOf(Tournament(id = 1L, name = "toto", createdAt = 1L, startTime = 2L, endTime = 3L))
        Mockito.`when`(tournamentDao.insertTournaments(tournaments))
            .thenReturn(Unit)

        val result = insertTournaments(tournaments)

        assertTrue(result.isSuccess)
        Mockito.verify(tournamentDao).insertTournaments(tournaments)
    }
}

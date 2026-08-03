/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.usecases.GetLineupById
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetLineupByIdTests {
    @Mock lateinit var lineupDao: LineupRepository
    lateinit var observeLineupById: GetLineupById

    @Before
    fun init() {
        observeLineupById = GetLineupById(lineupDao, testDispatcherProvider())
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val lineup = Lineup(id = 1L, name = "toto", teamId = 1L, tournamentId = 1L)
        Mockito.`when`(lineupDao.getLineupById(1L)).thenReturn(lineup)

        val result = observeLineupById(1L)

        assertTrue(result.isSuccess)
        Assert.assertEquals(lineup, result.getOrNull())
    }
}

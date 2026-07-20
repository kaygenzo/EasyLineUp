/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.GetLineupById
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetLineupByIdTests {
    @Mock lateinit var lineupDao: LineupRepository
    lateinit var getLineupById: GetLineupById

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getLineupById = GetLineupById(lineupDao, testDispatcherProvider())
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val lineup = Lineup(id = 1L, name = "toto", teamId = 1L, tournamentId = 1L)
        Mockito.`when`(lineupDao.getLineupByIdSingle(1L)).thenReturn(Single.just(lineup))

        val result = getLineupById(1L)

        assertTrue(result.isSuccess)
        Assert.assertEquals(lineup, result.getOrNull())
    }
}

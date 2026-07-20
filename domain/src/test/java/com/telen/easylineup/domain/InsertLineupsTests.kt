/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.InsertLineups
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
internal class InsertLineupsTests {
    @Mock lateinit var lineupDao: LineupRepository
    lateinit var insertLineups: InsertLineups

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertLineups = InsertLineups(lineupDao, testDispatcherProvider())
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val lineups = listOf(Lineup(id = 1L, name = "toto", teamId = 1L, tournamentId = 1L))
        Mockito.`when`(lineupDao.insertLineups(lineups)).thenReturn(Unit)

        val result = insertLineups(lineups)

        assertTrue(result.isSuccess)
        Mockito.verify(lineupDao).insertLineups(lineups)
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.ObserveLineupById
import io.reactivex.rxjava3.core.Flowable
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
internal class ObserveLineupByIdTests {
    @Mock lateinit var lineupDao: LineupRepository
    lateinit var observeLineupById: ObserveLineupById

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        observeLineupById = ObserveLineupById(lineupDao)
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val lineup = Lineup()
        Mockito.`when`(lineupDao.getLineupById(1L)).thenReturn(Flowable.just(lineup))

        val result = observeLineupById(1L).toList()

        assertEquals(listOf(lineup), result)
    }
}

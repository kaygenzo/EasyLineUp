/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.DeleteLineup
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.*

@RunWith(MockitoJUnitRunner::class)
internal class DeleteLineupTests {
    private val extraHitters = 0

    @Mock
    lateinit var dao: LineupRepository
    lateinit var deleteLineup: DeleteLineup
    lateinit var lineup1: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deleteLineup = DeleteLineup(dao, testDispatcherProvider())

        lineup1 =
                Lineup(1, "test1", 1, 1, MODE_DISABLED, TeamStrategy.STANDARD.id, extraHitters, 3L)

        Mockito.`when`(dao.getLineupByIdSingle(1)).thenReturn(Single.just(lineup1))
        Mockito.`when`(dao.getLineupByIdSingle(2)).thenReturn(Single.error(Exception()))
    }

    @Test
    fun shouldReturnAnExceptionIfLineupIdIsNull() = runTest {
        val result = deleteLineup(null)
        assertTrue(result.isFailure)
    }

    @Test
    fun shouldDeleteLineupIfIdExists() = runTest {
        Mockito.`when`(dao.deleteLineup(lineup1)).thenReturn(Completable.complete())
        val result = deleteLineup(1)
        assertTrue(result.isSuccess)
    }

    @Test
    fun shouldDeleteLineupIfIdNotExists() = runTest {
        val result = deleteLineup(2)
        assertTrue(result.isFailure)
    }

    @Test
    fun shouldReturnAnErrorIfLineupExistsButCannotBeDeleted() = runTest {
        Mockito.`when`(dao.deleteLineup(lineup1)).thenReturn(Completable.error(Exception()))
        val result = deleteLineup(1)
        assertTrue(result.isFailure)
    }
}

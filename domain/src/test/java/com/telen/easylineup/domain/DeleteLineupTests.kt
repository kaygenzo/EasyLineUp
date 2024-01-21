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
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.*

@RunWith(MockitoJUnitRunner::class)
internal class DeleteLineupTests {
    private val extraHitters = 0
    private val observer: TestObserver<DeleteLineup.ResponseValue> = TestObserver()

    @Mock
    lateinit var dao: LineupRepository
    lateinit var deleteLineup: DeleteLineup
    lateinit var lineup1: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deleteLineup = DeleteLineup(dao)

        lineup1 =
                Lineup(1, "test1", 1, 1, MODE_DISABLED, TeamStrategy.STANDARD.id, extraHitters, 3L)

        Mockito.`when`(dao.getLineupByIdSingle(1)).thenReturn(Single.just(lineup1))
        Mockito.`when`(dao.getLineupByIdSingle(2)).thenReturn(Single.error(Exception()))
    }

    @Test
    fun shouldReturnAnExceptionIfLineupIdIsNull() {
        deleteLineup.executeUseCase(DeleteLineup.RequestValues(null)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldDeleteLineupIfIdExists() {
        Mockito.`when`(dao.deleteLineup(lineup1)).thenReturn(Completable.complete())
        deleteLineup.executeUseCase(DeleteLineup.RequestValues(1)).subscribe(observer)
        observer.await()
        observer.assertComplete()
    }

    @Test
    fun shouldDeleteLineupIfIdNotExists() {
        deleteLineup.executeUseCase(DeleteLineup.RequestValues(2)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldReturnAnErrorIfLineupExistsButCannotBeDeleted() {
        Mockito.`when`(dao.deleteLineup(lineup1)).thenReturn(Completable.error(Exception()))
        deleteLineup.executeUseCase(DeleteLineup.RequestValues(1)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }
}

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
import org.mockito.junit.*
import org.mockito.*

@RunWith(MockitoJUnitRunner::class)
internal class DeleteLineupTests {

    @Mock
    lateinit var dao: LineupRepository

    lateinit var mDeleteLineup: DeleteLineup
    lateinit var lineup1: Lineup
    private val extraHitters = 0

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mDeleteLineup = DeleteLineup(dao)

        lineup1 = Lineup(1, "test1", 1, 1, MODE_DISABLED, TeamStrategy.STANDARD.id, extraHitters, 3L)

        Mockito.`when`(dao.getLineupByIdSingle(1)).thenReturn(Single.just(lineup1))
        Mockito.`when`(dao.getLineupByIdSingle(2)).thenReturn(Single.error(Exception()))
    }

    @Test
    fun shouldReturnAnExceptionIfLineupIDIsNull() {
        val observer = TestObserver<DeleteLineup.ResponseValue>()
        mDeleteLineup.executeUseCase(DeleteLineup.RequestValues(null)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldDeleteLineupIfIdExists() {
        Mockito.`when`(dao.deleteLineup(lineup1)).thenReturn(Completable.complete())
        val observer = TestObserver<DeleteLineup.ResponseValue>()
        mDeleteLineup.executeUseCase(DeleteLineup.RequestValues(1)).subscribe(observer)
        observer.await()
        observer.assertComplete()
    }

    @Test
    fun shouldDeleteLineupIfIdNotExists() {
        val observer = TestObserver<DeleteLineup.ResponseValue>()
        mDeleteLineup.executeUseCase(DeleteLineup.RequestValues(2)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldReturnAnErrorIfLineupExistsButCannotBeDeleted() {
        Mockito.`when`(dao.deleteLineup(lineup1)).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<DeleteLineup.ResponseValue>()
        mDeleteLineup.executeUseCase(DeleteLineup.RequestValues(1)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

}
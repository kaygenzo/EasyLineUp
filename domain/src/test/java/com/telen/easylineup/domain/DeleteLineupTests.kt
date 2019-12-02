package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.model.MODE_NONE
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.*
import org.mockito.*

@RunWith(MockitoJUnitRunner::class)
class DeleteLineupTests {

    @Mock
    lateinit var dao: LineupDao

    lateinit var mDeleteLineup: DeleteLineup
    lateinit var lineup1: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mDeleteLineup = DeleteLineup(dao)

        lineup1 = Lineup(1, "test1", 1, 1, MODE_NONE)

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
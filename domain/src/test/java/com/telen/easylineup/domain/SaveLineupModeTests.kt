package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.data.Lineup
import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.MODE_DH
import com.telen.easylineup.repository.data.MODE_NONE
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.*



@RunWith(MockitoJUnitRunner::class)
class SaveLineupModeTests {

    @Mock lateinit var lineupDao: LineupDao
    lateinit var mSaveLineupMode: SaveLineupMode
    lateinit var lineup: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSaveLineupMode = SaveLineupMode(lineupDao)

        lineup = Lineup(1, "test1", 1, 1, MODE_NONE)

        Mockito.`when`(lineupDao.getLineupByIdSingle(1)).thenReturn(Single.just(lineup))
        Mockito.`when`(lineupDao.getLineupByIdSingle(2)).thenReturn(Single.error(Exception()))
    }

    @Test
    fun shouldReturnAnExceptionIfLineupIDIsNull() {
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(null, MODE_NONE)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldReturnAnExceptionIfLineupIDNotExists() {
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(2, MODE_NONE)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldReturnAnExceptionIfUpdateFail() {
        Mockito.`when`(lineupDao.updateLineup(lineup)).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(1, MODE_NONE)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldSaveTheLineupModeNone() {
        Mockito.`when`(lineupDao.updateLineup(lineup)).thenReturn(Completable.complete())
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(1, MODE_NONE)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updateLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(MODE_NONE, it.mode)
        })
    }

    @Test
    fun shouldSaveTheLineupModeDH() {
        Mockito.`when`(lineupDao.updateLineup(lineup)).thenReturn(Completable.complete())

        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(1, MODE_DH)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updateLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(MODE_DH, it.mode)
        })
    }
}
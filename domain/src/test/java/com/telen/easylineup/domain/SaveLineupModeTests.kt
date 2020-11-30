package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.SaveLineupMode
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
internal class SaveLineupModeTests {

    @Mock lateinit var lineupDao: LineupRepository
    lateinit var mSaveLineupMode: SaveLineupMode
    lateinit var lineup: Lineup
    private val extraHitters = 0

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSaveLineupMode = SaveLineupMode(lineupDao)

        lineup = Lineup(1, "test1", 1, 1, MODE_DISABLED, TeamStrategy.STANDARD.id, extraHitters, 3L)

        Mockito.`when`(lineupDao.getLineupByIdSingle(1)).thenReturn(Single.just(lineup))
        Mockito.`when`(lineupDao.getLineupByIdSingle(2)).thenReturn(Single.error(Exception()))
    }

    @Test
    fun shouldReturnAnExceptionIfLineupIDIsNull() {
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(null, MODE_DISABLED)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldReturnAnExceptionIfLineupIDNotExists() {
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(2, MODE_DISABLED)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldReturnAnExceptionIfUpdateFail() {
        Mockito.`when`(lineupDao.updateLineup(lineup)).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(1, MODE_DISABLED)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldSaveTheLineupModeNone() {
        Mockito.`when`(lineupDao.updateLineup(lineup)).thenReturn(Completable.complete())
        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(1, MODE_DISABLED)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updateLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(MODE_DISABLED, it.mode)
        })
    }

    @Test
    fun shouldSaveTheLineupModeDH() {
        Mockito.`when`(lineupDao.updateLineup(lineup)).thenReturn(Completable.complete())

        val observer = TestObserver<SaveLineupMode.ResponseValue>()
        mSaveLineupMode.executeUseCase(SaveLineupMode.RequestValues(1, MODE_ENABLED)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updateLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(MODE_ENABLED, it.mode)
        })
    }
}
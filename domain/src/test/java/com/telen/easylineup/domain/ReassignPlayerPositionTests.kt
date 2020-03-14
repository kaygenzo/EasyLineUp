package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ReassignPlayerPositionTests {

    @Mock lateinit var playerFieldPositionsDao: PlayerFieldPositionsDao
    lateinit var mReassignPlayerPosition: ReassignPlayerPosition

    private lateinit var player1: PlayerWithPosition

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mReassignPlayerPosition = ReassignPlayerPosition(playerFieldPositionsDao)

        player1 = PlayerWithPosition(
                "toto", 1, 1, 1,
                null, FieldPosition.CATCHER.position, 0f,0f, 1, 1,
                1, 1, 1)

        Mockito.`when`(playerFieldPositionsDao.updatePlayerFieldPosition(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnErrorIfUpdateFail() {
        Mockito.`when`(playerFieldPositionsDao.updatePlayerFieldPosition(any())).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<ReassignPlayerPosition.ResponseValue>()
        mReassignPlayerPosition.executeUseCase(ReassignPlayerPosition.RequestValues(player1, FieldPosition.CATCHER))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldUpdatePlayerFieldPosition() {
        val observer = TestObserver<ReassignPlayerPosition.ResponseValue>()
        mReassignPlayerPosition.executeUseCase(ReassignPlayerPosition.RequestValues(player1, FieldPosition.FIRST_BASE))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerFieldPositionsDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(FieldPosition.FIRST_BASE.position, it.position)
        })
    }
}
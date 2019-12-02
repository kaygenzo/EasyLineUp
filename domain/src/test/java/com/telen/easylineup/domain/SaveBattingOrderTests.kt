package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SaveBattingOrderTests {

    @Mock lateinit var lineupDao: PlayerFieldPositionsDao
    lateinit var mSaveBattingOrder: SaveBattingOrder
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        mSaveBattingOrder = SaveBattingOrder(lineupDao)
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.PITCHER.position, 0f, 0f, 0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.position, 0f, 0f, 2, 2, 2, 1, 1))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, 9, 3, 3, 1, 1))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, 10, 4, 4, 1, 1))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 1))
    }

    @Test
    fun shouldNotSaveBattersWhichAreSubstituteOrNoOrder() {
        Mockito.`when`(lineupDao.updatePlayerFieldPositions(any())).thenReturn(Completable.complete())
        val observer = TestObserver<SaveBattingOrder.ResponseValue>()
        mSaveBattingOrder.executeUseCase(SaveBattingOrder.RequestValues(players)).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(3, it.size)
            Assert.assertEquals(0, it.filter { it.id == 5L }.size)
            Assert.assertEquals(0, it.filter { it.id == 1L }.size)
        })
    }

    @Test
    fun shouldTriggerAnErrorIfCannotSavePosition() {
        Mockito.`when`(lineupDao.updatePlayerFieldPositions(any())).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<SaveBattingOrder.ResponseValue>()
        mSaveBattingOrder.executeUseCase(SaveBattingOrder.RequestValues(players)).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }
}
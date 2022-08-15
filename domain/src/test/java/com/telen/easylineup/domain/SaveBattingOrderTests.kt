package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.SaveBattingOrder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SaveBattingOrderTests {

    @Mock lateinit var lineupDao: PlayerFieldPositionRepository
    lateinit var mSaveBattingOrder: SaveBattingOrder
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        mSaveBattingOrder = SaveBattingOrder(lineupDao)
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.PITCHER.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, 0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,2, 2, 2, 1, 1))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,9, 3, 3, 1, 1))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,10, 4, 4, 1, 1))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 1))
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
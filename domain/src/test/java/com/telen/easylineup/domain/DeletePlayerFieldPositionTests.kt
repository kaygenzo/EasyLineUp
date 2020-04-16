package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
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
class DeletePlayerFieldPositionTests {
    lateinit var deletePlayerFieldPosition: DeletePlayerFieldPosition
    @Mock lateinit var lineupDao: PlayerFieldPositionsDao
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deletePlayerFieldPosition = DeletePlayerFieldPosition(lineupDao)
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.PITCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_FLEX,0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,2, 0, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,9, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,10, 0, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))
        players.add(PlayerWithPosition("toutou", 6, 6, 1, null,
                FieldPosition.DP_DH.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE, 8, 6, 6, 1, 16))

        Mockito.`when`(lineupDao.deletePositions(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnExceptionIfListIsEmpty() {
        val lineupMode = MODE_ENABLED
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(mutableListOf(), FieldPosition.PITCHER, lineupMode))
                .subscribe(observer)
        observer.await()
        observer.assertError(NoSuchElementException::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfNoMatchingPlayerAndPosition() {
        val lineupMode = MODE_ENABLED
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, FieldPosition.SECOND_BASE, lineupMode))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfAnErrorOccurredDuringDeletion() {
        val lineupMode = MODE_ENABLED
        Mockito.`when`(lineupDao.deletePositions(any())).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, FieldPosition.PITCHER, lineupMode))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldDeletePlayerFieldPositions_dp_and_flex_if_flex() {
        val lineupMode = MODE_ENABLED
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, FieldPosition.PITCHER, lineupMode))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it[0].id)
            Assert.assertEquals(6, it[1].id)
        })
    }

    @Test
    fun shouldDeletePlayerFieldPositions_dp_and_flex_if_dp() {
        val lineupMode = MODE_ENABLED
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, FieldPosition.DP_DH, lineupMode))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it[0].id)
            Assert.assertEquals(6, it[1].id)
        })
    }

    @Test
    fun shouldDeletePlayerFieldPosition_one_position_if_not_dp_nor_flex() {
        val lineupMode = MODE_ENABLED
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, FieldPosition.CATCHER, lineupMode))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it[0].id)
            Assert.assertEquals(1, it.size)
        })
    }
}
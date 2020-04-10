package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
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
                FieldPosition.PITCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,2, 0, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,9, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE,10, 0, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))
    }

    @Test
    fun shouldTriggerAnExceptionIfListIsEmpty() {
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(mutableListOf(), players.first().toPlayer(), FieldPosition.PITCHER))
                .subscribe(observer)
        observer.await()
        observer.assertError(NoSuchElementException::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfNoMatchingPlayerAndPosition() {
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, players.first().toPlayer(), FieldPosition.CATCHER))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfAnErrorOccurredDuringDeletion() {
        Mockito.`when`(lineupDao.deletePosition(any())).thenReturn(Completable.error(Exception()))
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, players.first().toPlayer(), FieldPosition.PITCHER))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldDeletePlayerFieldPosition() {
        Mockito.`when`(lineupDao.deletePosition(any())).thenReturn(Completable.complete())
        val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, players.first().toPlayer(), FieldPosition.PITCHER))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePosition(any())
    }
}
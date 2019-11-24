package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.repository.Constants
import com.telen.easylineup.repository.data.*
import io.reactivex.Completable
import io.reactivex.Single
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
class SavePlayerFieldPositionTests {

    lateinit var savePlayerFieldPosition: SavePlayerFieldPosition
    @Mock lateinit var lineupDao: LineupDao
    lateinit var players: MutableList<PlayerWithPosition>
    var observer = TestObserver<SavePlayerFieldPosition.ResponseValue>()
    var newPlayer = Player(6, 1, "tyty", 6, 6, null, 128)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        savePlayerFieldPosition = SavePlayerFieldPosition(lineupDao)
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.PITCHER.position, 0f, 0f, 0, 1, 1, 1,1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.position, 0f, 0f, 2, 2, 2, 1,2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.position, 0f, 0f, 4, 3, 3, 1,4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, 6, 4, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.position, 0f, 0f, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1,16))
        Mockito.`when`(lineupDao.insertPlayerFieldPosition(any())).thenReturn(Single.just(6))
        Mockito.`when`(lineupDao.updatePlayerFieldPosition(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIDIsNull() {
        val fieldPosition = FieldPosition.SUBSTITUTE
        val isNewPosition = true
        val mode = MODE_NONE
        val request = SavePlayerFieldPosition.RequestValues(null, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
        verify(lineupDao, never()).insertPlayerFieldPosition(any())
    }

    @Test
    fun shouldTriggerAnErrorIfErrorOccurredDuringInsert() {
        Mockito.`when`(lineupDao.insertPlayerFieldPosition(any())).thenReturn(Single.error(Exception()))

        val fieldPosition = FieldPosition.SUBSTITUTE
        val isNewPosition = true
        val mode = MODE_NONE
        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldTriggerAnErrorIfErrorOccurredDuringUpdate() {
        Mockito.`when`(lineupDao.updatePlayerFieldPosition(any())).thenReturn(Completable.error(Exception()))

        val fieldPosition = FieldPosition.SUBSTITUTE
        val isNewPosition = false
        val mode = MODE_NONE
        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
        verify(lineupDao, never()).insertPlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_200_BecauseIsSubstituteAndNewPosition() {
        val fieldPosition = FieldPosition.SUBSTITUTE
        val isNewPosition = true
        val mode = MODE_NONE
        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(Constants.SUBSTITUTE_ORDER_VALUE, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_10_BecauseIsPitcherWithDHAndNewPosition() {
        val fieldPosition = FieldPosition.PITCHER
        val isNewPosition = true
        val mode = MODE_DH
        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(Constants.ORDER_PITCHER_WHEN_DH, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_1_BecauseIsPitcherWithNoneLineupModeAndNewPosition() {
        val fieldPosition = FieldPosition.PITCHER
        val isNewPosition = true
        val mode = MODE_NONE
        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_3_BecauseIsPlayerWithNoneLineupModeAndNewPosition() {
        val fieldPosition = FieldPosition.PITCHER
        val isNewPosition = true
        val mode = MODE_NONE
        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        players.add(PlayerWithPosition("test", 7, 7, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, 1, 7, 7, 1,16))

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(3, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_5_BecauseIsPitcherWithNoneLineupModeAndNewPosition() {
        val fieldPosition = FieldPosition.SECOND_BASE
        val isNewPosition = true
        val mode = MODE_NONE
        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition, 0f, 0f, players, mode, isNewPosition)

        players.add(PlayerWithPosition("test", 7, 7, 1, null,
                FieldPosition.FIRST_BASE.position, 0f, 0f, 1, 7, 7, 1,16))
        players.add(PlayerWithPosition("test2", 8, 8, 1, null,
                FieldPosition.THIRD_BASE.position, 0f, 0f, 3, 8, 8, 1,16))

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(5, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldUpdatePositionWithNewPlayerId() {
        val fieldPosition = FieldPosition.FIRST_BASE
        val isNewPosition = false

        val request = SavePlayerFieldPosition.RequestValues(1, newPlayer, fieldPosition,
                1f,2f, players, MODE_NONE, isNewPosition)

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao, never()).insertPlayerFieldPosition(any())
        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(newPlayer.id, it.playerId)
            Assert.assertEquals(1f, it.x)
            Assert.assertEquals(2f, it.y)
        })
    }
}
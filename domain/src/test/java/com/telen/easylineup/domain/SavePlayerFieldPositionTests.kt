package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.SavePlayerFieldPosition
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
internal class SavePlayerFieldPositionTests {

    lateinit var savePlayerFieldPosition: SavePlayerFieldPosition
    @Mock lateinit var lineupDao: PlayerFieldPositionRepository
    lateinit var players: MutableList<PlayerWithPosition>
    var observer = TestObserver<SavePlayerFieldPosition.ResponseValue>()
    var newPlayer = Player(6, 1, "tyty", 6, 6, null, 128)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        savePlayerFieldPosition = SavePlayerFieldPosition(lineupDao)
        players = mutableListOf()
        players.add(PlayerWithPosition("toto", 1, 1, 1, null,
                FieldPosition.PITCHER.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,0, 1, 1, 1, 1))
        players.add(PlayerWithPosition("tata", 2, 2, 1, null,
                FieldPosition.CATCHER.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,2, 2, 2, 1, 2))
        players.add(PlayerWithPosition("titi", 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,4, 3, 3, 1, 4))
        players.add(PlayerWithPosition("tutu", 4, 4, 1, null,
                FieldPosition.FIRST_BASE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,6, 4, 4, 1, 8))
        players.add(PlayerWithPosition("tete", 5, 5, 1, null,
                FieldPosition.SUBSTITUTE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, Constants.SUBSTITUTE_ORDER_VALUE, 5, 5, 1, 16))
        Mockito.`when`(lineupDao.insertPlayerFieldPosition(any())).thenReturn(Single.just(6))
        Mockito.`when`(lineupDao.updatePlayerFieldPosition(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIDIsNull() {
        val fieldPosition = FieldPosition.SUBSTITUTE
        val mode = MODE_DISABLED
        val teamType = TeamType.BASEBALL.id
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = null,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

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
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldTriggerAnErrorIfErrorOccurredDuringUpdate() {
        Mockito.`when`(lineupDao.updatePlayerFieldPosition(any())).thenReturn(Completable.error(Exception()))

        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
        verify(lineupDao, never()).insertPlayerFieldPosition(any())
    }

    //// ORDER /////

    @Test
    fun shouldInsertPlayerWithSortOrder_200_BecauseIsSubstituteAndNewPosition() {
        val fieldPosition = FieldPosition.SUBSTITUTE
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(Constants.SUBSTITUTE_ORDER_VALUE, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_10_BecauseIs_Pitcher_MODE_ENABLED_BASEBALL_positionEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        players.removeIf { it.position == FieldPosition.PITCHER.id }

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(TeamStrategy.STANDARD.getDesignatedPlayerOrder(), it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithNextAvailableOrder_BecauseIs_Pitcher_MODE_ENABLED_SOFTBALL_positionEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.SOFTBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        players.removeIf { it.position == FieldPosition.PITCHER.id }

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_10_BecauseIs_Pitcher_MODE_ENABLED_BASEBALL_positionNotEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(TeamStrategy.STANDARD.getDesignatedPlayerOrder(), it.order)
        })
        verify(lineupDao, never()).insertPlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithNextAvailableOrder_BecauseIs_Pitcher_MODE_ENABLED_SOFTBALL_positionNotEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.SOFTBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.order)
        })
        verify(lineupDao, never()).insertPlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPitcherWithSortOrder_1_Because_MODE_DISABLED() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        players.removeIf { it.position == FieldPosition.PITCHER.id }

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldInsertPlayerWithSortOrder_3_BecauseIs_Player_MODE_DISABLED() {
        val fieldPosition = FieldPosition.SECOND_BASE
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        players.add(PlayerWithPosition("test", 7, 7, 1, null,
                FieldPosition.THIRD_BASE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE,1, 7, 7, 1, 16))

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(3, it.order)
        })
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
    }

    @Test
    fun shouldKeepOrderOfOtherPlayer() {
        val fieldPosition = FieldPosition.CENTER_FIELD
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(4, it.order)
        })
    }

    //// FLAGS /////

    @Test
    fun shouldAdd_FlagFlex_BecauseIs_Pitcher_MODE_ENABLED_BASEBALL_positionEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        players.removeIf { it.position == FieldPosition.PITCHER.id }

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, it.flags)
        })
    }

    @Test
    fun shouldAdd_FlagNone_BecauseIs_Pitcher_MODE_ENABLED_SOFTBALL_positionEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.SOFTBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        players.removeIf { it.position == FieldPosition.PITCHER.id }

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it.flags)
        })
    }

    @Test
    fun shouldAdd_FlagFlex_BecauseIs_Pitcher_MODE_ENABLED_BASEBALL_positionNotEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, it.flags)
        })
    }

    @Test
    fun shouldAdd_FlagFlex_BecauseIs_Pitcher_MODE_ENABLED_SOFTBALL_positionNotEmpty() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.SOFTBALL.id
        val mode = MODE_ENABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it.flags)
        })
    }

    @Test
    fun shouldAdd_FlagNone_BecauseIs_Pitcher_MODE_DISABLED() {
        val fieldPosition = FieldPosition.PITCHER
        val teamType = TeamType.BASEBALL.id
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = mode
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, it.flags)
        })
    }

    //// POSITION /////

    @Test
    fun shouldUpdatePositionWithNewPlayerId() {
        val fieldPosition = FieldPosition.FIRST_BASE
        val teamType = TeamType.BASEBALL.id
        val strategy = TeamStrategy.STANDARD

        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType,
                lineupMode = MODE_DISABLED,
                strategy = strategy
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao, never()).insertPlayerFieldPosition(any())
        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(newPlayer.id, it.playerId)
            var coordinate = FieldPosition.getPositionCoordinates(FieldPosition.FIRST_BASE, strategy)
            Assert.assertEquals(coordinate.x, it.x)
            Assert.assertEquals(coordinate.y, it.y)
        })
    }
}
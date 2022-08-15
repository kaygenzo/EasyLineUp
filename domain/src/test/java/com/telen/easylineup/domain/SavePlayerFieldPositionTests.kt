package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.SavePlayerFieldPosition
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

////////////// NO HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionBaseballStandardTests: SavePlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 0)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballStandardTests: SavePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 0)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballSlowpitchTests: SavePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
        TeamStrategy.SLOWPITCH.batterSize, 0)

////////////// CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionBaseballCustomStandardTests: SavePlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballCustomStandardTests: SavePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballCustomSlowpitchTests: SavePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
        TeamStrategy.SLOWPITCH.batterSize, 3)

@RunWith(MockitoJUnitRunner::class)
internal abstract class SavePlayerFieldPositionTests(val teamType: TeamType, val strategy: TeamStrategy, val batterSize: Int, val extraHitterSize: Int) {

    lateinit var savePlayerFieldPosition: SavePlayerFieldPosition
    @Mock lateinit var lineupDao: PlayerFieldPositionRepository
    lateinit var players: MutableList<PlayerWithPosition>
    var observer = TestObserver<SavePlayerFieldPosition.ResponseValue>()

    val newPlayer = Player(9, 1, "t9", 9, 9, null, 0x07)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        savePlayerFieldPosition = SavePlayerFieldPosition(lineupDao)

        players = mutableListOf()
        var i = 1
        teamType.getValidPositions(strategy).forEach {
            players.add(PlayerWithPosition(
                    playerName = "t${i}",
                    shirtNumber = i,
                    licenseNumber = i.toLong(),
                    teamId = 1,
                    image = null,
                    position = it.id,
                    x = 0f, y = 0f,
                    flags = PlayerFieldPosition.FLAG_NONE,
                    order = batterSize - i + 1,
                    fieldPositionID = i.toLong(),
                    playerID = i.toLong(),
                    lineupId = 1,
                    playerPositions = 1)
            )
            i++
        }

        Mockito.`when`(lineupDao.insertPlayerFieldPosition(any())).thenReturn(Single.just(9))
        Mockito.`when`(lineupDao.updatePlayerFieldPosition(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldTriggerAnErrorIfLineupIDIsNull() {
        val fieldPosition = FieldPosition.SUBSTITUTE
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = null,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
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
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
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
        val mode = MODE_DISABLED
        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
        verify(lineupDao, never()).insertPlayerFieldPosition(any())
    }

    //// ORDER /////

    @Test
    fun shouldInsertPlayerWhenOrderAvailable() {
        players.removeAt(5)

        val fieldPosition = FieldPosition.SHORT_STOP
        val mode = MODE_DISABLED

        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(batterSize - 5, it.order)
            Assert.assertEquals(9, it.playerId)
        })
    }

    @Test
    fun shouldInsertSubstituteWhenOrderAvailable() {

        players.removeAt(players.size - 1)

        val fieldPosition = FieldPosition.SUBSTITUTE
        val mode = MODE_DISABLED

        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            if(extraHitterSize < 1) {
                Assert.assertEquals(200, it.order)
            }
            else
                Assert.assertEquals(1, it.order)
            Assert.assertEquals(9, it.playerId)
        })
    }

    @Test
    fun shouldInsertSubstituteWhenOrderAvailableAndExtraHitterFilled() {
        players.removeAt(players.size - 1)
        for(i in (batterSize+1)..(batterSize+extraHitterSize+1)) {
            players.add(PlayerWithPosition("t${i}", i, i.toLong(), 1L, null,
                    FieldPosition.SUBSTITUTE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, i,
                    i.toLong(), i.toLong(), 1L, i))
        }

        val fieldPosition = FieldPosition.SUBSTITUTE
        val mode = MODE_DISABLED

        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(200, it.order)
            Assert.assertEquals(9, it.playerId)
        })
    }

    @Test
    fun shouldInsertSubstituteWhenBattersCompleteAndExtraHitterAvailable() {
        for(i in 10..10) {
            players.add(PlayerWithPosition("t${i}", i, i.toLong(), 1L, null,
                    FieldPosition.SUBSTITUTE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, i,
                    i.toLong(), i.toLong(), 1L, i))
        }

        val fieldPosition = FieldPosition.SUBSTITUTE
        val mode = MODE_DISABLED

        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            if(extraHitterSize < 1) {
                Assert.assertEquals(200, it.order)
            }
            else {
                Assert.assertEquals(11, it.order)
            }
            Assert.assertEquals(9, it.playerId)
        })
    }

    @Test
    fun shouldInsertPitcherIntoBaseballStrategyAndLineupModeEnabled() {
        players.removeAt(0)
        val fieldPosition = FieldPosition.PITCHER
        val mode = MODE_ENABLED

        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = newPlayer,
                position = fieldPosition,
                players = players,
                teamType = TeamType.BASEBALL.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(lineupDao, never()).updatePlayerFieldPosition(any())
        verify(lineupDao).insertPlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(strategy.getDesignatedPlayerOrder(extraHitterSize), it.order)
            Assert.assertEquals(9, it.playerId)
        })
    }

    @Test
    fun shouldUpdatePlayerIfPositionAlreadyExists() {
        val fieldPosition = FieldPosition.SHORT_STOP
        val mode = MODE_DISABLED

        val request = SavePlayerFieldPosition.RequestValues(
                lineupID = 1,
                player = Player(42L, 1L, "test", 42, 42L, null, 0),
                position = fieldPosition,
                players = players,
                teamType = teamType.id,
                lineupMode = mode,
                strategy = strategy,
                batterSize = batterSize,
                extraHittersSize = extraHitterSize
        )

        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(lineupDao, never()).insertPlayerFieldPosition(any())
        verify(lineupDao).updatePlayerFieldPosition(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals( batterSize - 5, it.order)
            Assert.assertEquals(42, it.playerId)
        })
    }
}
package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.lang.IllegalArgumentException

////////////// NO HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionBaseballStandardTests: DeletePlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 0)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballStandardTests: DeletePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 0)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballSlowpitchTests: DeletePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
        TeamStrategy.SLOWPITCH.batterSize, 0)

////////////// CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionBaseballCustomStandardTests: DeletePlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballCustomStandardTests: DeletePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballCustomSlowpitchTests: DeletePlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
        TeamStrategy.SLOWPITCH.batterSize, 3)

@RunWith(MockitoJUnitRunner::class)
internal abstract class DeletePlayerFieldPositionTests(private val teamType: TeamType, private val strategy: TeamStrategy, private val batterSize: Int, private val extraHitterSize: Int) {
    lateinit var deletePlayerFieldPosition: DeletePlayerFieldPosition
    @Mock lateinit var lineupDao: PlayerFieldPositionRepository
    lateinit var players: MutableList<PlayerWithPosition>
    val lineupMode = MODE_ENABLED
    val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deletePlayerFieldPosition = DeletePlayerFieldPosition(lineupDao)
        players = mutableListOf()
        var i = 1
        teamType.getValidPositions(strategy).forEach {
            players.add(createPlayerWithPosition(i, PlayerFieldPosition.FLAG_NONE, it))
            i++
        }

        Mockito.`when`(lineupDao.deletePositions(any())).thenReturn(Completable.complete())
        Mockito.`when`(lineupDao.updatePlayerFieldPositions(any())).thenReturn(Completable.complete())
    }

    private fun createPlayerWithPosition(index: Int, flag: Int, position: FieldPosition, order: Int = index): PlayerWithPosition {
        return PlayerWithPosition("t${index}", index, index.toLong(), 1L, null,
                position.id, 0f, 0f, flag, order,
                index.toLong(), index.toLong(), 1L, index)
    }

    @Test
    fun shouldTriggerAnExceptionIfListIsEmpty() {
        val player = Player(1, 1,"", 1, 1L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(mutableListOf(), player, FieldPosition.PITCHER, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertError(NoSuchElementException::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfAnErrorOccurredDuringDeletion() {
        Mockito.`when`(lineupDao.deletePositions(any())).thenReturn(Completable.error(Exception()))
        val player = Player(1, 1,"", 1, 1L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, player, FieldPosition.PITCHER, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertError(Exception::class.java)
    }

    @Test
    fun shouldTriggerAnErrorIfAnErrorOccurredDuringUpdate() {
        Mockito.`when`(lineupDao.updatePlayerFieldPositions(any())).thenReturn(Completable.error(IllegalArgumentException()))

        for(i in (batterSize+1)..(batterSize+extraHitterSize+4)) {
            players.add(createPlayerWithPosition(i, PlayerFieldPosition.FLAG_NONE, FieldPosition.SUBSTITUTE))
        }

        val player = Player((batterSize+1).toLong(), 1,"", 1, 1L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, player, FieldPosition.SUBSTITUTE, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldDeletePlayerFieldPositions_dp_and_flex_if_delete_flex() {

        players[0].position = FieldPosition.DP_DH.id
        players.add(createPlayerWithPosition(batterSize+1, PlayerFieldPosition.FLAG_FLEX, FieldPosition.PITCHER))

        val player = Player((batterSize+1).toLong(), 1,"", 1, 1L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, player, FieldPosition.PITCHER, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it[0].id)
            Assert.assertEquals((batterSize+1).toLong(), it[1].id)
            Assert.assertEquals(2, it.size)
        })
    }

    @Test
    fun shouldDeletePlayerFieldPositions_dp_and_flex_if_delete_dp() {

        players[0].position = FieldPosition.DP_DH.id
        players.add(createPlayerWithPosition(batterSize+1, PlayerFieldPosition.FLAG_FLEX, FieldPosition.PITCHER))

        val player = Player(1, 1,"", 6, 6L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, player, FieldPosition.DP_DH, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(1, it[0].id)
            Assert.assertEquals((batterSize+1).toLong(), it[1].id)
            Assert.assertEquals(2, it.size)
        })
    }

    @Test
    fun shouldDeletePlayerFieldPosition_one_position_if_not_dp_nor_flex() {
        val player = Player(2, 1,"", 2, 2L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, player, FieldPosition.CATCHER, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(2, it[0].id)
            Assert.assertEquals(1, it.size)
        })
    }

    @Test
    fun shouldDeletePlayerFieldPosition_if_multiple_substitutes_player_not_first() {

        for(i in (batterSize+1)..(batterSize+extraHitterSize+1)) {
            players.add(createPlayerWithPosition(i, PlayerFieldPosition.FLAG_NONE, FieldPosition.SUBSTITUTE))
        }

        val player = Player((batterSize+extraHitterSize+1).toLong(), 1,"", 9, 9L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, player, FieldPosition.SUBSTITUTE, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals((batterSize+extraHitterSize+1).toLong(), it[0].id)
            Assert.assertEquals(1, it.size)
        })
    }

    @Test
    fun shouldShiftSubstitutes() {
        //added 2 substitutes as batters
        for(i in (batterSize-1)..batterSize) {
            players[i-1].position = FieldPosition.SUBSTITUTE.id
        }
        //added 2 substitutes as non batters
        for(i in (batterSize+1)..(batterSize+extraHitterSize+1)) {
            players.add(createPlayerWithPosition(i, PlayerFieldPosition.FLAG_NONE, FieldPosition.SUBSTITUTE, Constants.SUBSTITUTE_ORDER_VALUE))
        }

        val player = Player((batterSize-1).toLong(), 1,"", 9, 9L, null, 1)
        deletePlayerFieldPosition.executeUseCase(DeletePlayerFieldPosition.RequestValues(players, player, FieldPosition.SUBSTITUTE, lineupMode, extraHitterSize))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupDao).deletePositions(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals((batterSize-1).toLong(), it[0].id)
            Assert.assertEquals(1, it.size)
        })

        if(extraHitterSize > 0) {
            verify(lineupDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
                Assert.assertEquals(batterSize - 1, it[0].order)
                Assert.assertEquals((batterSize + 1).toLong(), it[0].id)
                Assert.assertEquals(1, it.size)
            })
        }
        else {
            verify(lineupDao).updatePlayerFieldPositions(com.nhaarman.mockitokotlin2.check {
                Assert.assertEquals(true, it.isEmpty())
            })
        }
    }
}
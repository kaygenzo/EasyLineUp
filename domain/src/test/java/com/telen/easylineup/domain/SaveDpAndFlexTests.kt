/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.isCatcher
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SaveDpAndFlexTests : BaseUseCaseTests() {
    private val strategy = TeamStrategy.STANDARD
    private val extraHitters = 0
    private val lineup = Lineup(strategy = strategy.id, extraHitters = extraHitters)
    private val observer: TestObserver<SaveDpAndFlex.ResponseValue> = TestObserver()
    private lateinit var useCase: SaveDpAndFlex
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        val teamId = 1L

        useCase = SaveDpAndFlex()

        val noFlag = PlayerFieldPosition.FLAG_NONE
        players = mutableListOf(
            generate(1L, FieldPosition.PITCHER, noFlag, 1),
            generate(2L, FieldPosition.RIGHT_FIELD, noFlag, 2),
            generate(3L, FieldPosition.DP_DH, noFlag, 3),
            generate(4L, FieldPosition.SHORT_STOP, noFlag, 4),
            generate(5L, FieldPosition.SUBSTITUTE, noFlag, Constants.SUBSTITUTE_ORDER_VALUE)
        )
    }

    private fun startUseCase(
        dp: Player?,
        flex: Player?,
        players: List<PlayerWithPosition> = this.players,
        exception: Class<out Throwable>? = null
    ) {
        val playersSize = players.size
        val request = SaveDpAndFlex.RequestValues(lineup, dp, flex, players)
        useCase.executeUseCase(request).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
            Assert.assertEquals("Size of player list must not change", playersSize, players.size)
        }
    }

    @Test
    fun shouldTriggerAnErrorIfDpNotAssigned() {
        startUseCase(
            dp = null,
            flex = players.first().toPlayer(),
            players = players,
            NeedAssignBothPlayersException::class.java
        )
    }

    @Test
    fun shouldTriggerAnErrorIfFlexNotAssigned() {
        startUseCase(
            dp = players.first().toPlayer(),
            flex = null,
            players = players,
            NeedAssignBothPlayersException::class.java
        )
    }

    @Test
    fun shouldChangeFlagAndOrderOfTheFlex() {
        val dp = players[3]
        val flex = players[1]
        startUseCase(dp = dp.toPlayer(), flex = flex.toPlayer(), players = players)

        Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, flex.flags)
        Assert.assertEquals(
            TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters),
            flex.order
        )
    }

    @Test
    fun shouldChangeFlagAndOrderOfOldFlex() {
        players.forEach { it.flags = PlayerFieldPosition.FLAG_FLEX }

        val dp = players[3]
        val flex = players[1]
        startUseCase(dp = dp.toPlayer(), flex = flex.toPlayer(), players = players)

        // flex
        Assert.assertEquals(1, players.count { it.isFlex() })
        Assert.assertEquals(flex.playerId, players.firstOrNull { it.isFlex() }?.playerId)
        Assert.assertEquals(
            TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters),
            flex.order
        )

        // others
        Assert.assertEquals(4, players.count { !it.isFlex() })
        Assert.assertArrayEquals(
            longArrayOf(1L, 3L, 4L, 5L),
            players.filter { !it.isFlex() }.map { it.playerId }.sorted().toLongArray()
        )
    }

    @Test
    fun shouldAssignDpToExistingPosition() {
        val dp = players[3]
        val flex = players[1]
        startUseCase(dp = dp.toPlayer(), flex = flex.toPlayer(), players = players)

        Assert.assertEquals(1, players.count { it.isDpDh() })
        Assert.assertEquals(dp.playerId, players.firstOrNull { it.isDpDh() }?.playerId)
        Assert.assertEquals(2, players.firstOrNull { it.isDpDh() }?.order)
    }

    @Test
    fun shouldAssignDpToNotExistingPosition() {
        players.removeIf { it.isDpDh() }
        val dp = players[3]
        val flex = players[1]
        startUseCase(dp = dp.toPlayer(), flex = flex.toPlayer(), players = players)

        Assert.assertEquals(1, players.count { it.isDpDh() })
        Assert.assertEquals(dp.playerId, players.firstOrNull { it.isDpDh() }?.playerId)
        Assert.assertEquals(2, players.firstOrNull { it.isDpDh() }?.order)
    }

    @Test
    fun shouldReplaceBatterOrderWhenSwitchFlex() {
        players.clear()
        players.addAll(generateFullLineup(lineup, strategy, withDpDh = true))
        val dp = players.first { it.isDpDh() }.toPlayer()
        val oldFlex = players.first { it.isFlex() }
        val newFlex = players.first { it.isCatcher() }.toPlayer()
        val oldCatcherOrder = players.first { it.isCatcher() }.order

        startUseCase(dp = dp, flex = newFlex, players = players)
        Assert.assertEquals(
            strategy.getDesignatedPlayerOrder(lineup.extraHitters),
            players.first { it.isFlex() }.order
        )
        Assert.assertEquals(newFlex.id, players.first { it.isFlex() }.playerId)
        Assert.assertEquals(PlayerFieldPosition.FLAG_NONE, oldFlex.flags)
        Assert.assertEquals(oldCatcherOrder, oldFlex.order)
    }
}

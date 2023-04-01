package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import com.telen.easylineup.domain.usecases.exceptions.FirstPositionEmptyException
import com.telen.easylineup.domain.usecases.exceptions.SamePlayerException
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
internal class SwitchPlayersPositionTests : BaseUseCaseTests() {

    lateinit var mSwitchPlayersPosition: SwitchPlayersPosition

    private lateinit var player2bis: PlayerWithPosition
    private val players = mutableListOf<PlayerWithPosition>()
    private val strategy = TeamStrategy.STANDARD
    private val extraHitters = 0
    private lateinit var lineup: Lineup
    private val observer = TestObserver<SwitchPlayersPosition.ResponseValue>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSwitchPlayersPosition = SwitchPlayersPosition()

        players.add(generate(1L, FieldPosition.PITCHER, PlayerFieldPosition.FLAG_FLEX, 10))
        players.add(generate(2L, FieldPosition.CATCHER, PlayerFieldPosition.FLAG_NONE, 2))
        players.add(generate(3L, FieldPosition.CENTER_FIELD, PlayerFieldPosition.FLAG_NONE, 4))
        players.add(generate(4L, FieldPosition.FIRST_BASE, PlayerFieldPosition.FLAG_NONE, 6))
        val order = Constants.SUBSTITUTE_ORDER_VALUE
        players.add(generate(5L, FieldPosition.SUBSTITUTE, PlayerFieldPosition.FLAG_NONE, order))
        players.add(generate(6L, FieldPosition.DP_DH, PlayerFieldPosition.FLAG_NONE, 8))

        player2bis = generate(2L, FieldPosition.CATCHER, PlayerFieldPosition.FLAG_NONE, 2)
        lineup = Lineup(strategy = strategy.id, extraHitters = extraHitters)
    }

    private fun startUseCase(
        fromPosition: FieldPosition,
        toPosition: FieldPosition,
        lineupMode: Boolean,
        teamType: TeamType,
        players: List<PlayerWithPosition> = this.players,
        exception: Class<out Throwable>? = null
    ) {
        lineup.mode = if (lineupMode) MODE_ENABLED else MODE_DISABLED
        val request = SwitchPlayersPosition.RequestValues(
            players,
            fromPosition,
            toPosition,
            teamType.id,
            lineup
        )
        val playersSize = players.size
        val originalPlayers = players.map { it.copy() }
        mSwitchPlayersPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
            Assert.assertEquals("Size of player list must not change", playersSize, players.size)
            Assert.assertEquals(
                originalPlayers.first { it.position == fromPosition.id }.playerID,
                players.first { it.position == toPosition.id }.playerID
            )
            // if the destination is not empty, we can check the players are switched
            originalPlayers.firstOrNull { it.position == toPosition.id }?.let {
                Assert.assertEquals(
                    it.playerID,
                    players.first { it.position == fromPosition.id }.playerID
                )
            }
        }
    }

    @Test
    fun shouldTriggerSamePlayersException() {
        startUseCase(
            FieldPosition.PITCHER,
            FieldPosition.PITCHER,
            true,
            TeamType.BASEBALL,
            exception = SamePlayerException::class.java
        )
    }

    @Test
    fun shouldTriggerFirstPositionEmptyIfFirstPositionIsNotAssigned() {
        startUseCase(
            FieldPosition.SHORT_STOP,
            FieldPosition.PITCHER,
            false,
            TeamType.BASEBALL,
            exception = FirstPositionEmptyException::class.java
        )
    }

    /////// BASEBALL TESTS /////////

    @Test
    fun shouldSwitch_baseball_Another_Player_With_Another_Player() {
        startUseCase(FieldPosition.FIRST_BASE, FieldPosition.CENTER_FIELD, true, TeamType.BASEBALL)
        assertVerification(4, 4, FieldPosition.CENTER_FIELD.id, PlayerFieldPosition.FLAG_NONE, 6)
        assertVerification(3, 3, FieldPosition.FIRST_BASE.id, PlayerFieldPosition.FLAG_NONE, 4)
    }

    @Test
    fun shouldSwitch_baseball_DH_and_Pitcher() {
        startUseCase(FieldPosition.DP_DH, FieldPosition.PITCHER, true, TeamType.BASEBALL)
        assertVerification(
            6,
            6,
            FieldPosition.PITCHER.id,
            PlayerFieldPosition.FLAG_FLEX,
            TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        )
        assertVerification(1, 1, FieldPosition.DP_DH.id, PlayerFieldPosition.FLAG_NONE, 8)
    }

    @Test
    fun shouldSwitch_baseball_DH_and_Another_Player() {
        startUseCase(FieldPosition.DP_DH, FieldPosition.FIRST_BASE, true, TeamType.BASEBALL)
        assertVerification(6, 6, FieldPosition.FIRST_BASE.id, PlayerFieldPosition.FLAG_NONE, 8)
        assertVerification(4, 4, FieldPosition.DP_DH.id, PlayerFieldPosition.FLAG_NONE, 6)
    }

    @Test
    fun shouldSwitch_baseball_Pitcher_and_Another_Player() {
        startUseCase(FieldPosition.PITCHER, FieldPosition.FIRST_BASE, true, TeamType.BASEBALL)
        assertVerification(1, 1, FieldPosition.FIRST_BASE.id, PlayerFieldPosition.FLAG_NONE, 6)
        assertVerification(
            4,
            4,
            FieldPosition.PITCHER.id,
            PlayerFieldPosition.FLAG_FLEX,
            TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        )
    }

    @Test
    fun shouldSwitch_baseball_Pitcher_and_Empty() {
        startUseCase(FieldPosition.PITCHER, FieldPosition.SECOND_BASE, true, TeamType.BASEBALL)
        assertVerification(1, 1, FieldPosition.SECOND_BASE.id, PlayerFieldPosition.FLAG_NONE, 1)
        Assert.assertNull(players.firstOrNull { it.isPitcher() })
    }

    @Test
    fun shouldSwitch_baseball_DH_and_Empty() {
        startUseCase(FieldPosition.DP_DH, FieldPosition.SECOND_BASE, true, TeamType.BASEBALL)
        assertVerification(6, 6, FieldPosition.SECOND_BASE.id, PlayerFieldPosition.FLAG_NONE, 8)
        Assert.assertNull(players.firstOrNull { it.isDpDh() })
    }

    @Test
    fun shouldSwitch_baseball_Another_and_Empty() {
        startUseCase(FieldPosition.FIRST_BASE, FieldPosition.SECOND_BASE, true, TeamType.BASEBALL)
        assertVerification(4, 4, FieldPosition.SECOND_BASE.id, PlayerFieldPosition.FLAG_NONE, 6)
        Assert.assertNull(players.firstOrNull { it.isFirstBase() })
    }

    //////// SOFTBALL TESTS /////////

    /*
    * Rules:
    * - Flex can be anyone except DP.
    * - If FLEX exists, its order is 10
    * - If 2 players switch their positions:
    *   if it is 2 normal players, only positions change
    *   if it is a FLEX and a normal player, only positions change
    *   if it is the DP and a normal player, only positions change
    *   if is is the DP and the FLEX, switch position, flags and order
    */

    @Test
    fun shouldSwitch_softball_normal_with_normal() {
        startUseCase(FieldPosition.FIRST_BASE, FieldPosition.CENTER_FIELD, true, TeamType.SOFTBALL)
        assertVerification(4, 4, FieldPosition.CENTER_FIELD.id, PlayerFieldPosition.FLAG_NONE, 6)
        assertVerification(3, 3, FieldPosition.FIRST_BASE.id, PlayerFieldPosition.FLAG_NONE, 4)
    }

    @Test
    fun shouldSwitch_softball_flex_with_normal() {
        players.first { it.isPitcher() }.apply {
            flags = PlayerFieldPosition.FLAG_NONE
            order = 1
        }

        players.first { it.isCatcher() }.apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        }

        startUseCase(FieldPosition.CATCHER, FieldPosition.CENTER_FIELD, true, TeamType.SOFTBALL)
        assertVerification(
            2,
            2,
            FieldPosition.CENTER_FIELD.id,
            PlayerFieldPosition.FLAG_FLEX,
            TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        )
        assertVerification(3, 3, FieldPosition.CATCHER.id, PlayerFieldPosition.FLAG_NONE, 4)
    }

    @Test
    fun shouldSwitch_softball_dp_with_normal() {
        startUseCase(FieldPosition.DP_DH, FieldPosition.CENTER_FIELD, true, TeamType.SOFTBALL)
        assertVerification(6, 6, FieldPosition.CENTER_FIELD.id, PlayerFieldPosition.FLAG_NONE, 8)
        assertVerification(3, 3, FieldPosition.DP_DH.id, PlayerFieldPosition.FLAG_NONE, 4)
    }

    @Test
    fun shouldSwitch_softball_flex_with_dp() {
        players.first { it.isPitcher() }.apply {
            flags = PlayerFieldPosition.FLAG_NONE
            order = 1
        }

        players.first { it.isCatcher() }.apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        }

        startUseCase(FieldPosition.CATCHER, FieldPosition.DP_DH, true, TeamType.SOFTBALL)
        assertVerification(2, 2, FieldPosition.DP_DH.id, PlayerFieldPosition.FLAG_NONE, 8)
        assertVerification(
            6,
            6,
            FieldPosition.CATCHER.id,
            PlayerFieldPosition.FLAG_FLEX,
            TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        )
    }

    @Test
    fun shouldSwitch_softball_dp_with_flex() {
        players.first { it.isPitcher() }.apply {
            flags = PlayerFieldPosition.FLAG_NONE
            order = 1
        }

        players.first { it.isCatcher() }.apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        }

        startUseCase(FieldPosition.DP_DH, FieldPosition.CATCHER, true, TeamType.SOFTBALL)
        assertVerification(
            6,
            6,
            FieldPosition.CATCHER.id,
            PlayerFieldPosition.FLAG_FLEX,
            TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        )
        assertVerification(2, 2, FieldPosition.DP_DH.id, PlayerFieldPosition.FLAG_NONE, 8)
    }

    private fun assertVerification(
        id: Long,
        playerID: Long,
        position: Int,
        flags: Int,
        order: Int
    ) {
        players.first { it.position == position }.let {
            Assert.assertEquals(id, it.fieldPositionID)
            Assert.assertEquals(playerID, it.playerID)
            Assert.assertEquals(position, it.position)
            Assert.assertEquals(flags, it.flags)
            Assert.assertEquals(order, it.order)
        }
    }
}
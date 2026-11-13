/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isBattingSubstitute
import com.telen.easylineup.domain.model.isDpDhOrFlex
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

////////////// NO HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionBaseballStandardTests : DeletePlayerFieldPositionTests(
    TeamType.BASEBALL, TeamStrategy.STANDARD, 0
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballStandardTests : DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.STANDARD, 0
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionBaseball5ManTests : DeletePlayerFieldPositionTests(
    TeamType.BASEBALL, TeamStrategy.FIVE_MAN_STANDARD, 0
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballSlowpitchTests : DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.SLOWPITCH, 0
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftball5ManTests : DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD, 0
)

////////////// CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionBaseballCustomStandardTests :
    DeletePlayerFieldPositionTests(
    TeamType.BASEBALL, TeamStrategy.STANDARD, 3
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballCustomStandardTests :
    DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.STANDARD, 3
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionBaseballCustom5ManTests : DeletePlayerFieldPositionTests(
    TeamType.BASEBALL, TeamStrategy.FIVE_MAN_STANDARD, 3
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballCustomSlowpitchTests :
    DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.SLOWPITCH, 3
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballCustom5ManTests : DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD, 3
)

@RunWith(MockitoJUnitRunner::class)
internal abstract class DeletePlayerFieldPositionTests(
    private val teamType: TeamType,
    private val strategy: TeamStrategy,
    private val extraHitterSize: Int
) : BaseUseCaseTests() {
    private val lineupMode = MODE_ENABLED
    private lateinit var deletePlayerFieldPosition: DeletePlayerFieldPosition
    private lateinit var players: MutableList<PlayerWithPosition>
    private val substituteId = 42L

    /** The substitute as the list holds it now: players are immutable, the list changes. */
    private val substitute: PlayerWithPosition
        get() = players.first { it.playerId == substituteId }
    private lateinit var player: Player

    @Before
    fun init() {
        deletePlayerFieldPosition = DeletePlayerFieldPosition(testDispatcherProvider())
        players = mutableListOf()
        teamType.getValidPositions(strategy).forEachIndexed { index, pos ->
            val id = index + 1
            players.add(generate(id.toLong(), pos, PlayerFieldPosition.FLAG_NONE, id))
        }
        player = Player(69, 1, "69", 69, "69")
        players.add(
            generate(substituteId, FieldPosition.SUBSTITUTE, PlayerFieldPosition.FLAG_NONE, 42)
        )
    }

    private suspend fun startUseCase(
        players: List<PlayerWithPosition> = this.players,
        player: Player,
        exception: Class<out Throwable>? = null
    ) {
        val playersSize = players.size
        val target = players.firstOrNull { it.playerId == player.id }
        val wasSubstitute = target?.isSubstitute() ?: false
        val wasDpOrFlex = target?.isDpDhOrFlex() ?: false
        val previousOrder = target?.order ?: 0
        val battingSubstitutes = players.count { it.isBattingSubstitute() }
        val original = players.toList()
        val result = deletePlayerFieldPosition(players, player, lineupMode, extraHitterSize)
        exception?.let {
            Assert.assertTrue(result.isFailure)
            Assert.assertEquals(it, result.exceptionOrNull()?.javaClass)
        } ?: let {
            Assert.assertTrue(result.isSuccess)
            Assert.assertEquals("The input list must not change", original, players)
            val updated = result.getOrThrow()
            Assert.assertEquals("Size of player list must not change", playersSize, updated.size)
            if (players === this.players) {
                this.players.clear()
                this.players.addAll(updated)
            }
            updated.first { it.playerId == player.id }.let {
                // A field player goes back to the bench as a batter when an extra-hitter slot
                // is free (keeping their spot), bench-only otherwise.
                val expectedOrder = when {
                    wasSubstitute || wasDpOrFlex -> 0
                    previousOrder in 1 until Constants.SUBSTITUTE_ORDER_VALUE &&
                        battingSubstitutes < extraHitterSize -> previousOrder
                    else -> Constants.SUBSTITUTE_ORDER_VALUE
                }
                Assert.assertEquals("The player batting order", expectedOrder, it.order)
                Assert.assertEquals(
                    "The player position is reset",
                    // a substitute being deleted is fully unassigned (-1), a field player
                    // being deleted is only demoted to the bench (SUBSTITUTE.id)
                    if (wasSubstitute) -1 else FieldPosition.SUBSTITUTE.id,
                    it.position
                )
            }
        }
    }

    private fun assertPlayerDeleted(player: PlayerWithPosition) {
        Assert.assertEquals(FieldPosition.SUBSTITUTE.id, player.position)
        Assert.assertEquals(0, player.order)
    }

    private fun assertPlayerNotDeleted(player: PlayerWithPosition) {
        Assert.assertNotEquals(FieldPosition.SUBSTITUTE.id, player.position)
        Assert.assertNotEquals(0, player.order)
    }

    private fun setupDpAndFlex() {
        players.edit({ it.isPitcher() }) { reset() }
        players.edit({ it.playerId == substituteId }) {
            copy(position = FieldPosition.DP_DH.id, order = 2_000)
        }
        players.add(
            generate(
                player.id,
                FieldPosition.PITCHER,
                PlayerFieldPosition.FLAG_FLEX,
                player.id.toInt()
            )
        )
    }

    @Test
    fun shouldTriggerAnExceptionIfListIsEmpty() = runTest {
        startUseCase(mutableListOf(), player, NoSuchElementException::class.java)
    }

    @Test
    fun shouldDeletePlayerFieldPositionsDpAndFlexIfDeleteFlex() = runTest {
        setupDpAndFlex()
        startUseCase(players, player)
        assertPlayerDeleted(players.first { it.playerId == player.id })
        assertPlayerDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPositionsDpAndFlexIfDeleteDp() = runTest {
        setupDpAndFlex()
        startUseCase(players, substitute.toPlayer())
        assertPlayerDeleted(players.first { it.playerId == player.id })
        assertPlayerDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPositionOnePositionIfNotDpNorFlex() = runTest {
        setupDpAndFlex()
        startUseCase(players, players.first { it.isShortStop() }.toPlayer())
        assertPlayerNotDeleted(players.first { it.playerId == player.id })
        assertPlayerNotDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPositionIfMultipleSubstitutesPlayerNotFirst() = runTest {
        for (i in (strategy.batterSize + 1)..(strategy.batterSize + extraHitterSize + 1)) {
            players.add(
                generate(
                    i.toLong(),
                    FieldPosition.SUBSTITUTE,
                    PlayerFieldPosition.FLAG_NONE,
                    i
                )
            )
        }
        startUseCase(players, players.last().toPlayer())
        Assert.assertEquals(
            "Only one substitute must be deleted",
            1,
            players.filter { it.order == 0 }.size
        )
    }

    @Test
    fun shouldReplaceOldSubstituteByAnewOne() = runTest {
        // added 2 substitutes as batters
        for (i in (strategy.batterSize - 1)..strategy.batterSize) {
            players.edit(i - 1) { copy(position = FieldPosition.SUBSTITUTE.id) }
        }
        // added 2 substitutes as non batters
        for (i in (strategy.batterSize + 1)..(strategy.batterSize + extraHitterSize + 1)) {
            players.add(
                generate(
                    i.toLong(),
                    FieldPosition.SUBSTITUTE,
                    PlayerFieldPosition.FLAG_NONE,
                    Constants.SUBSTITUTE_ORDER_VALUE
                )
            )
        }

        val player = Player((strategy.batterSize - 1).toLong(), 1, "", 9, "9", null, 1)
        val playerOrder = players.first { it.playerId == player.id }.order
        startUseCase(players, player)

        if (extraHitterSize > 0) {
            Assert.assertEquals(
                (strategy.batterSize + 1).toLong(),
                players.first { it.order == playerOrder }.playerId
            )
            Assert.assertEquals(
                FieldPosition.SUBSTITUTE.id,
                players.first { it.order == playerOrder }.position
            )
        } else {
            Assert.assertEquals(0, players.filter { it.order == playerOrder }.size)
        }
    }
    @Test
    fun shouldKeepRemovedFieldPlayerBattingWhenAnExtraHitterSlotIsFree() = runTest {
        // bench-only: the slot is free
        players.edit({ it.playerId == substituteId }) {
            copy(order = Constants.SUBSTITUTE_ORDER_VALUE)
        }
        val removed = players.first { it.isShortStop() }
        val order = removed.order
        val result = deletePlayerFieldPosition(players, removed.toPlayer(), MODE_DISABLED, 1)

        Assert.assertTrue(result.isSuccess)
        val shortStop = result.getOrThrow().first { it.playerId == removed.playerId }
        Assert.assertEquals(FieldPosition.SUBSTITUTE.id, shortStop.position)
        Assert.assertEquals(order, shortStop.order)
        Assert.assertTrue(shortStop.isBattingSubstitute())
    }

    @Test
    fun shouldBenchRemovedFieldPlayerWhenNoExtraHitterSlotIsFree() = runTest {
        // the only extra-hitter slot is already taken by a batting substitute
        players.edit({ it.playerId == substituteId }) { copy(order = strategy.batterSize + 1) }
        val removed = players.first { it.isShortStop() }
        val result = deletePlayerFieldPosition(players, removed.toPlayer(), MODE_DISABLED, 1)

        Assert.assertTrue(result.isSuccess)
        val shortStop = result.getOrThrow().first { it.playerId == removed.playerId }
        Assert.assertEquals(FieldPosition.SUBSTITUTE.id, shortStop.position)
        Assert.assertEquals(Constants.SUBSTITUTE_ORDER_VALUE, shortStop.order)
        Assert.assertFalse(shortStop.isBattingSubstitute())
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.isShortStop
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
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
    val observer: TestObserver<DeletePlayerFieldPosition.ResponseValue> = TestObserver()
    private lateinit var deletePlayerFieldPosition: DeletePlayerFieldPosition
    private lateinit var players: MutableList<PlayerWithPosition>
    private lateinit var substitute: PlayerWithPosition
    private lateinit var player: Player

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deletePlayerFieldPosition = DeletePlayerFieldPosition()
        players = mutableListOf()
        teamType.getValidPositions(strategy).forEachIndexed { index, pos ->
            val id = index + 1
            players.add(generate(id.toLong(), pos, PlayerFieldPosition.FLAG_NONE, id))
        }
        substitute = generate(42, FieldPosition.SUBSTITUTE, PlayerFieldPosition.FLAG_NONE, 42)
        player = Player(69, 1, "69", 69, 69L)
        players.add(substitute)
    }

    private fun startUseCase(
        players: List<PlayerWithPosition> = this.players,
        player: Player,
        exception: Class<out Throwable>? = null
    ) {
        val request =
            DeletePlayerFieldPosition.RequestValues(players, player, lineupMode, extraHitterSize)
        val playersSize = players.size
        deletePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
            Assert.assertEquals("Size of player list must not change", playersSize, players.size)
            players.first { it.playerId == player.id }.let {
                Assert.assertEquals("The player batting order is reset", 0, it.order)
                Assert.assertEquals("The player position is reset", -1, it.position)
            }
        }
    }

    private fun assertPlayerDeleted(player: PlayerWithPosition) {
        Assert.assertEquals(-1, player.position)
        Assert.assertEquals(0, player.order)
    }

    private fun assertPlayerNotDeleted(player: PlayerWithPosition) {
        Assert.assertNotEquals(-1, player.position)
        Assert.assertNotEquals(0, player.order)
    }

    private fun setupDpAndFlex() {
        players.first { it.isPitcher() }.reset()
        substitute.apply {
            position = FieldPosition.DP_DH.id
            order = 2_000
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
    fun shouldTriggerAnExceptionIfListIsEmpty() {
        startUseCase(mutableListOf(), player, NoSuchElementException::class.java)
    }

    @Test
    fun shouldDeletePlayerFieldPositionsDpAndFlexIfDeleteFlex() {
        setupDpAndFlex()
        startUseCase(players, player)
        assertPlayerDeleted(players.first { it.playerId == player.id })
        assertPlayerDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPositionsDpAndFlexIfDeleteDp() {
        setupDpAndFlex()
        startUseCase(players, substitute.toPlayer())
        assertPlayerDeleted(players.first { it.playerId == player.id })
        assertPlayerDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPositionOnePositionIfNotDpNorFlex() {
        setupDpAndFlex()
        startUseCase(players, players.first { it.isShortStop() }.toPlayer())
        assertPlayerNotDeleted(players.first { it.playerId == player.id })
        assertPlayerNotDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPositionIfMultipleSubstitutesPlayerNotFirst() {
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
            players.filter { it.position == -1 }.size
        )
    }

    @Test
    fun shouldReplaceOldSubstituteByAnewOne() {
        // added 2 substitutes as batters
        for (i in (strategy.batterSize - 1)..strategy.batterSize) {
            players[i - 1].position = FieldPosition.SUBSTITUTE.id
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

        val player = Player((strategy.batterSize - 1).toLong(), 1, "", 9, 9L, null, 1)
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
}

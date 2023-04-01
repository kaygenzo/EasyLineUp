package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.*
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
    TeamType.BASEBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 0
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballStandardTests : DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 0
)

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballSlowpitchTests : DeletePlayerFieldPositionTests(
    TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
    TeamStrategy.SLOWPITCH.batterSize, 0
)

////////////// CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionBaseballCustomStandardTests :
    DeletePlayerFieldPositionTests(
        TeamType.BASEBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3
    )

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballCustomStandardTests :
    DeletePlayerFieldPositionTests(
        TeamType.SOFTBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3
    )

@RunWith(MockitoJUnitRunner::class)
internal class DeletePlayerFieldPositionSoftballCustomSlowpitchTests :
    DeletePlayerFieldPositionTests(
        TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
        TeamStrategy.SLOWPITCH.batterSize, 3
    )

@RunWith(MockitoJUnitRunner::class)
internal abstract class DeletePlayerFieldPositionTests(
    private val teamType: TeamType,
    private val strategy: TeamStrategy,
    private val batterSize: Int,
    private val extraHitterSize: Int
) : BaseUseCaseTests() {
    private lateinit var deletePlayerFieldPosition: DeletePlayerFieldPosition
    private lateinit var players: MutableList<PlayerWithPosition>
    private lateinit var substitute: PlayerWithPosition
    private lateinit var player: Player
    private val lineupMode = MODE_ENABLED
    val observer = TestObserver<DeletePlayerFieldPosition.ResponseValue>()

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
            players.first { it.playerID == player.id }.let {
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
            order = 2000
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
    fun shouldDeletePlayerFieldPositions_dp_and_flex_if_delete_flex() {
        setupDpAndFlex()
        startUseCase(players, player)
        assertPlayerDeleted(players.first { it.playerID == player.id })
        assertPlayerDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPositions_dp_and_flex_if_delete_dp() {
        setupDpAndFlex()
        startUseCase(players, substitute.toPlayer())
        assertPlayerDeleted(players.first { it.playerID == player.id })
        assertPlayerDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPosition_one_position_if_not_dp_nor_flex() {
        setupDpAndFlex()
        startUseCase(players, players.first { it.isShortStop() }.toPlayer())
        assertPlayerNotDeleted(players.first { it.playerID == player.id })
        assertPlayerNotDeleted(substitute)
    }

    @Test
    fun shouldDeletePlayerFieldPosition_if_multiple_substitutes_player_not_first() {
        for (i in (batterSize + 1)..(batterSize + extraHitterSize + 1)) {
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
    fun shouldReplaceOldSubstituteByANewOne() {
        //added 2 substitutes as batters
        for (i in (batterSize - 1)..batterSize) {
            players[i - 1].position = FieldPosition.SUBSTITUTE.id
        }
        //added 2 substitutes as non batters
        for (i in (batterSize + 1)..(batterSize + extraHitterSize + 1)) {
            players.add(
                generate(
                    i.toLong(),
                    FieldPosition.SUBSTITUTE,
                    PlayerFieldPosition.FLAG_NONE,
                    Constants.SUBSTITUTE_ORDER_VALUE
                )
            )
        }

        val player = Player((batterSize - 1).toLong(), 1, "", 9, 9L, null, 1)
        val playerOrder = players.first { it.playerID == player.id }.order
        startUseCase(players, player)

        if (extraHitterSize > 0) {
            Assert.assertEquals(
                (batterSize + 1).toLong(),
                players.first { it.order == playerOrder }.playerID
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
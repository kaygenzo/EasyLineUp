/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.isShortStop
import com.telen.easylineup.domain.model.reset
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

////////////// NO HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionBaseballStandardTests :
    AssignPlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.STANDARD, 0)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionBaseball5ManTests :
    AssignPlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.FIVE_MAN_STANDARD, 0)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballStandardTests :
    AssignPlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.STANDARD, 0)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftball5ManTests :
    AssignPlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD, 0)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballSlowpitchTests :
    AssignPlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.SLOWPITCH, 0)

////////////// CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionBaseballCustomStandardTests :
    AssignPlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.STANDARD, 3)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionBaseballCustom5ManTests :
    AssignPlayerFieldPositionTests(TeamType.BASEBALL, TeamStrategy.FIVE_MAN_STANDARD, 3)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballCustomStandardTests :
    AssignPlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.STANDARD, 3)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballCustom5ManTests :
    AssignPlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD, 3)

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerFieldPositionSoftballCustomSlowpitchTests :
    AssignPlayerFieldPositionTests(TeamType.SOFTBALL, TeamStrategy.SLOWPITCH, 3)

@RunWith(MockitoJUnitRunner::class)
internal abstract class AssignPlayerFieldPositionTests(
    private val teamType: TeamType,
    private val strategy: TeamStrategy,
    private val extraHitterSize: Int
) : BaseUseCaseTests() {
    private var observer: TestObserver<AssignPlayerFieldPosition.ResponseValue> = TestObserver()
    private val newPlayer = Player(2_000, 1, "k2000", 2_000, 2_000, null, 0x07)
    private val lineup = Lineup(strategy = this.strategy.id, extraHitters = extraHitterSize)
    private lateinit var savePlayerFieldPosition: AssignPlayerFieldPosition
    private lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        savePlayerFieldPosition = AssignPlayerFieldPosition()

        players = mutableListOf()
        teamType.getValidPositions(strategy).forEachIndexed { i, pos ->
            val index = i + 1
            players.add(
                generate(
                    index.toLong(),
                    pos,
                    PlayerFieldPosition.FLAG_NONE,
                    strategy.batterSize - index + 1
                )
            )
        }
        players.add(
            PlayerWithPosition(
                newPlayer.name,
                newPlayer.sex,
                newPlayer.shirtNumber,
                newPlayer.licenseNumber,
                newPlayer.teamId,
                newPlayer.image,
                playerId = newPlayer.id,
                playerPositions = newPlayer.positions,
                lineupId = lineup.id
            )
        )
    }

    private fun startUseCase(
        position: FieldPosition,
        mode: Int,
        player: Player = newPlayer,
        teamType: TeamType = this.teamType,
        exception: Class<out Throwable>? = null
    ) {
        lineup.mode = mode
        val request = AssignPlayerFieldPosition.RequestValues(
            player = player,
            position = position,
            lineup = lineup,
            players = players,
            teamType = teamType.id
        )
        val playersSize = players.size
        savePlayerFieldPosition.executeUseCase(request).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(it)
        } ?: let {
            observer.assertComplete()
            players.apply {
                Assert.assertEquals("Size of player list must not change", playersSize, size)
                Assert.assertEquals(
                    "Only one instance of new player is expected",
                    1,
                    filter { it.playerId == newPlayer.id }.size
                )
                // substitute is not unique, that is not true for other positions
                if (position != FieldPosition.SUBSTITUTE) {
                    Assert.assertEquals(
                        "Only one instance of position is expected",
                        1,
                        filter { it.position == position.id }.size
                    )
                    Assert.assertEquals(
                        "The player at position is the new player",
                        newPlayer.id,
                        first { it.position == position.id }.playerId
                    )
                } else {
                    Assert.assertEquals(
                        "The new player is set as substitute",
                        FieldPosition.SUBSTITUTE.id,
                        first { it.playerId == newPlayer.id }.position
                    )
                }
            }
        }
    }

    @Test
    fun shouldInsertPlayerWithWhenOrderAvailable() {
        val lastOrder = players.first { it.isShortStop() }.order
        players.first { it.isShortStop() }.reset()
        startUseCase(FieldPosition.SHORT_STOP, MODE_DISABLED)
        Assert.assertEquals(lastOrder, players.first { it.isShortStop() }.order)
    }

    @Test
    fun shouldInsertSubstituteWithOrderWhenOrderAvailable() {
        val lastOrder = players.first { it.isShortStop() }.order
        players.first { it.isShortStop() }.reset()
        startUseCase(FieldPosition.SUBSTITUTE, MODE_DISABLED)

        players.first { newPlayer.id == it.playerId }.let {
            if (extraHitterSize < 1) {
                Assert.assertEquals(200, it.order)
            } else {
                Assert.assertEquals(lastOrder, it.order)
            }
        }
    }

    @Test
    fun shouldNotInsertInBattingOrderIfBattersAndExtraHitterAllComplete() {
        // fill in with fake players
        for (i in (strategy.batterSize + 1)..(strategy.batterSize + extraHitterSize + 1)) {
            players.add(
                PlayerWithPosition(
                    "t$i", 0, i, i.toLong(), 1L, null,
                    FieldPosition.SUBSTITUTE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, i,
                    i.toLong(), i.toLong(), 1L, i
                )
            )
        }

        startUseCase(FieldPosition.SUBSTITUTE, MODE_DISABLED)
        Assert.assertEquals(200, players.first { it.playerId == newPlayer.id }.order)
    }

    @Test
    fun shouldInsertInBattingOrderIfBattersCompleteAndExtraHitterAvailable() {
        startUseCase(FieldPosition.SUBSTITUTE, MODE_DISABLED)
        players.first { newPlayer.id == it.playerId }.let {
            if (extraHitterSize < 1) {
                Assert.assertEquals(200, it.order)
            } else {
                Assert.assertEquals(teamType.getValidPositions(strategy).size + 1, it.order)
            }
        }
    }

    @Test
    fun shouldInsertPitcherAsDesignatedPitcherOrderInBaseballLineup() {
        players.first { it.isPitcher() }.reset()

        startUseCase(FieldPosition.PITCHER, MODE_ENABLED, teamType = TeamType.BASEBALL)

        Assert.assertEquals(
            strategy.getDesignatedPlayerOrder(extraHitterSize),
            players.first { it.isPitcher() }.order
        )
    }

    @Test
    fun shouldNewPlayerReplaceAnotherPlayerOnSamePosition() {
        startUseCase(FieldPosition.SHORT_STOP, MODE_DISABLED)
        Assert.assertEquals(strategy.batterSize - 5, players.first { it.isShortStop() }.order)
    }

    @Test
    fun shouldTriggerAnErrorIfNewPlayerNotPartOfPlayersList() {
        startUseCase(
            FieldPosition.SHORT_STOP,
            MODE_DISABLED,
            player = Player(42, 1L, "player42", 42, 42),
            exception = IllegalArgumentException::class.java
        )
    }
}

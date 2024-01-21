/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDpDhOrFlex
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.usecases.UpdatePlayersWithLineupMode
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class UpdatePlayersWithLineupModeTests {
    var observer: TestObserver<UpdatePlayersWithLineupMode.ResponseValue> = TestObserver()
    private val extraHitters = 0
    private val strategy = TeamStrategy.STANDARD
    private val lineup = Lineup(strategy = this.strategy.id, extraHitters = extraHitters)
    lateinit var updatePlayersWithLineupMode: UpdatePlayersWithLineupMode
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        updatePlayersWithLineupMode = UpdatePlayersWithLineupMode()
        players = mutableListOf()
        players.add(
            PlayerWithPosition(
                "toto", 0, 1, 1, 1, null,
                FieldPosition.SECOND_BASE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, 0, 1, 1, 1, 1
            )
        )
        players.add(
            PlayerWithPosition(
                "tata", 0, 2, 2, 1, null,
                FieldPosition.CATCHER.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, 2, 2, 2, 1, 2
            )
        )
        players.add(
            PlayerWithPosition(
                "titi", 0, 3, 3, 1, null,
                FieldPosition.CENTER_FIELD.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, 4, 3, 3, 1, 4
            )
        )
        players.add(
            PlayerWithPosition(
                "tutu", 0, 4, 4, 1, null,
                FieldPosition.FIRST_BASE.id, 0f, 0f, PlayerFieldPosition.FLAG_NONE, 6, 4, 4, 1, 8
            )
        )
        players.add(
            PlayerWithPosition(
                "tete",
                0,
                5,
                5,
                1,
                null,
                FieldPosition.SUBSTITUTE.id,
                0f,
                0f,
                PlayerFieldPosition.FLAG_NONE,
                Constants.SUBSTITUTE_ORDER_VALUE,
                5,
                5,
                1,
                16
            )
        )
    }

    private fun startUseCase(
        teamType: TeamType?,
        lineupMode: Boolean,
        players: List<PlayerWithPosition> = this.players,
        exception: Class<out Throwable>? = null
    ) {
        lineup.mode = if (lineupMode) MODE_ENABLED else MODE_DISABLED
        val playersSize = players.size
        updatePlayersWithLineupMode.executeUseCase(
            UpdatePlayersWithLineupMode.RequestValues(
                players,
                lineup,
                teamType?.id ?: 1_000
            )
        ).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
            Assert.assertEquals(playersSize, players.size)
        }
    }

    @Test
    fun shouldTriggerAnExceptionIfTeamTypeIsUnknown() {
        startUseCase(
            teamType = null,
            lineupMode = true,
            exception = IllegalArgumentException::class.java
        )
    }

    @Test
    fun shouldDoNothingIfListEmptyAndDpEnabled() {
        val playersCopy = players.map { it.copy() }
        startUseCase(players = mutableListOf(), lineupMode = true, teamType = TeamType.BASEBALL)
        Assert.assertArrayEquals(players.toTypedArray(), playersCopy.toTypedArray())
    }

    @Test
    fun shouldDoNothingIfListEmptyAndDpDisabled() {
        val playersCopy = players.map { it.copy() }
        startUseCase(players = mutableListOf(), lineupMode = false, teamType = TeamType.BASEBALL)
        Assert.assertArrayEquals(players.toTypedArray(), playersCopy.toTypedArray())
    }

    @Test
    fun shouldDoNothingIfDesignatedPlayerEnabledAndSoftballTeam() {
        val playersCopy = players.map { it.copy() }
        startUseCase(players = mutableListOf(), lineupMode = true, teamType = TeamType.SOFTBALL)
        Assert.assertArrayEquals(players.toTypedArray(), playersCopy.toTypedArray())
    }

    @Test
    fun shouldDoNothingIfDpEnabledAndNoPitcherAssigned() {
        val playersCopy = players.map { it.copy() }
        startUseCase(teamType = TeamType.BASEBALL, lineupMode = true)
        Assert.assertArrayEquals(players.toTypedArray(), playersCopy.toTypedArray())
    }

    @Test
    fun shouldDoNothingIfDpDisabledAndNoPitcherOrDpassigned() {
        val playersCopy = players.map { it.copy() }
        startUseCase(teamType = TeamType.BASEBALL, lineupMode = false)
        Assert.assertArrayEquals(players.toTypedArray(), playersCopy.toTypedArray())
    }

    @Test
    fun shouldUpdatePitcherOrderTo10IfAssignedAndDpEnabled() {
        players.first().position = FieldPosition.PITCHER.id
        startUseCase(TeamType.BASEBALL, lineupMode = true)
        val pitcher = players.first { it.isPitcher() }
        val expectedOrder = TeamStrategy.STANDARD.getDesignatedPlayerOrder(extraHitters)
        Assert.assertEquals(expectedOrder, pitcher.order)
        Assert.assertEquals(PlayerFieldPosition.FLAG_FLEX, pitcher.flags)
    }

    @Test
    fun shouldDeletePitcherAndDpifAssignedAndDpDisabled() {
        players.first().apply {
            position = FieldPosition.PITCHER.id
            flags = PlayerFieldPosition.FLAG_FLEX
        }
        players[1].position = FieldPosition.DP_DH.id

        startUseCase(TeamType.BASEBALL, lineupMode = false)

        Assert.assertNull(players.firstOrNull { it.isPitcher() })
        Assert.assertNull(players.firstOrNull { it.isDpDhOrFlex() })
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
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

@RunWith(MockitoJUnitRunner::class)
internal class GetListAvailablePlayersForSelectionTests : BaseUseCaseTests() {
    private val observer: TestObserver<List<PlayerWithPosition>> =
        TestObserver()
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getListAvailablePlayersForSelection: GetListAvailablePlayersForSelection
    lateinit var players: MutableList<PlayerWithPosition>
    lateinit var lineup: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        val getRoster = GetRoster(playerDao, lineupDao, GetTeam(teamDao, testSchedulersProvider()), testSchedulersProvider())
        getListAvailablePlayersForSelection = GetListAvailablePlayersForSelection(getRoster, testSchedulersProvider())

        val team = Team(id = 1L, name = "toto", main = true)
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(listOf(team)))

        lineup = Lineup(id = 10L, teamId = team.id, roster = "1;2;3;4;5")

        players = mutableListOf(
            generate(1L, FieldPosition.PITCHER, PlayerFieldPosition.FLAG_NONE, 0, 1),
            generate(2L, null, PlayerFieldPosition.FLAG_NONE, 2, 2),
            generate(3L, FieldPosition.CENTER_FIELD, PlayerFieldPosition.FLAG_NONE, 9, 4),
            generate(4L, null, PlayerFieldPosition.FLAG_NONE, 10, 8),
            generate(
                5L,
                FieldPosition.SUBSTITUTE,
                PlayerFieldPosition.FLAG_NONE,
                Constants.SUBSTITUTE_ORDER_VALUE,
                16
            )
        )

        val teamPlayers = mutableListOf(
            Player(id = 1L, teamId = team.id, name = "p1", shirtNumber = 1, licenseNumber = 1L),
            Player(id = 2L, teamId = team.id, name = "p2", shirtNumber = 2, licenseNumber = 2L),
            Player(id = 3L, teamId = team.id, name = "p3", shirtNumber = 3, licenseNumber = 3L),
            Player(id = 4L, teamId = team.id, name = "p4", shirtNumber = 4, licenseNumber = 4L),
            Player(id = 5L, teamId = team.id, name = "p5", shirtNumber = 5, licenseNumber = 5L)
        )

        Mockito.`when`(lineupDao.getLineupByIdSingle(lineup.id)).thenReturn(Single.just(lineup))
        Mockito.`when`(playerDao.getPlayersByTeamId(team.id)).thenReturn(Single.just(teamPlayers))
        Mockito.`when`(playerDao.getPlayersNumberOverlay(lineup.id))
            .thenReturn(Single.just(emptyList()))
    }

    private fun startUseCase(
        position: FieldPosition?,
        players: List<PlayerWithPosition> = this.players,
        exception: Class<out Throwable>? = null
    ) {
        getListAvailablePlayersForSelection(players, position, lineup).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
        }
    }

    @Test
    fun shouldTriggerAnErrorIfListEmpty() {
        startUseCase(
            players = mutableListOf(),
            position = FieldPosition.PITCHER,
            exception = NoSuchElementException::class.java
        )
    }

    @Test
    fun shouldOnlyReturnPlayersWithoutFieldPositionOrSubstitutes() {
        startUseCase(position = FieldPosition.PITCHER)
        observer.values().first().let {
            Assert.assertEquals(3, it.size)
            Assert.assertEquals(1, it.filter { it.playerId == 2L }.size)
            Assert.assertEquals(1, it.filter { it.playerId == 4L }.size)
            Assert.assertEquals(1, it.filter { it.playerId == 5L }.size)
        }
    }

    @Test
    fun shouldSortPlayersByFieldPositionCatcher() {
        startUseCase(position = FieldPosition.CATCHER)
        observer.values().first().let {
            Assert.assertEquals(2, it[0].playerId)
            Assert.assertEquals(4, it[1].playerId)
        }
    }

    @Test
    fun shouldSortPlayersByFieldPositionSecondBase() {
        startUseCase(position = FieldPosition.SECOND_BASE)
        observer.values().first().let {
            Assert.assertEquals(4, it[0].playerId)
            Assert.assertEquals(2, it[1].playerId)
        }
    }

    @Test
    fun shouldRtriggerAnExceptionWhenRosterIsEmpty() {
        lineup.roster = ""
        startUseCase(
            position = FieldPosition.SECOND_BASE,
            exception = NoSuchElementException::class.java
        )
    }

    @Test
    fun shouldReturnSomePlayersWhenRosterIsNotFull() {
        lineup.roster = "1;2;3"
        startUseCase(position = FieldPosition.SECOND_BASE)
        Assert.assertEquals(1, observer.values().first().size)
        Assert.assertEquals(2L, observer.values().first().first().playerId)
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import android.content.Context
import android.content.res.Resources
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.DeleteLineup
import com.telen.easylineup.domain.usecases.GetBattersState
import com.telen.easylineup.domain.usecases.GetDpAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import com.telen.easylineup.domain.usecases.UpdatePlayersWithLineupMode
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.nhaarman.mockitokotlin2.any
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

/**
 * Characterization tests for [LineupsInteractorImpl], written as a safety net before
 * merging the Interactor layer into the UseCase layer.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LineupsInteractorImplTest : BaseInteractorTest() {

    @Mock
    lateinit var playerRepository: PlayerRepository

    @Mock
    lateinit var lineupRepository: LineupRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var playerFieldPositionRepository: PlayerFieldPositionRepository

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var resources: Resources

    lateinit var interactor: LineupsInteractorImpl

    private val mainTeam = Team(id = 1L, name = "Panthers", type = TeamType.SOFTBALL.id, main = true)
    private val lineup = Lineup(
        id = 10L,
        name = "Game 1",
        teamId = mainTeam.id,
        tournamentId = 5L,
        strategy = TeamStrategy.STANDARD.id
    )

    private fun playerWithPosition(playerId: Long, position: Int, order: Int = 1): PlayerWithPosition {
        return PlayerWithPosition(
            playerName = "player$playerId",
            shirtNumber = playerId.toInt(),
            licenseNumber = playerId,
            teamId = mainTeam.id,
            image = null,
            position = position,
            x = 0f,
            y = 0f,
            flags = 0,
            order = order,
            fieldPositionId = 0L,
            playerId = playerId,
            lineupId = lineup.id,
            playerPositions = 1,
            playerSex = 0
        )
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(context.resources).thenReturn(resources)
        Mockito.`when`(resources.getStringArray(Mockito.anyInt()))
            .thenReturn(Array(20) { "position$it" })

        loadKoinModules(
            module {
                single { playerRepository }
                single { lineupRepository }
                single { GetTeam(teamRepository) }
                single { CreateLineup(lineupRepository) }
                single { UpdateLineupRoster(lineupRepository) }
                single { DeleteLineup(lineupRepository) }
                single { SetLineupMode() }
                single { UpdatePlayersWithLineupMode() }
                single { GetRoster(playerRepository, lineupRepository) }
                single { SaveBattingOrderAndPositions(lineupRepository, playerFieldPositionRepository) }
                single { GetDpAndFlexFromPlayersInField() }
                single { SaveDpAndFlex() }
                single { GetBattersState() }
                single { GetListAvailablePlayersForSelection() }
                single { GetOnlyPlayersInField() }
                single { UpdateLineup(lineupRepository) }
                single { UpdatePlayersWithBatters() }
            }
        )

        Mockito.`when`(teamRepository.getTeamsRx()).thenReturn(Single.just(listOf(mainTeam)))

        interactor = LineupsInteractorImpl(context)
    }

    @Test
    fun shouldDelegateInsertLineupsToRepository() {
        Mockito.`when`(lineupRepository.insertLineups(listOf(lineup)))
            .thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.insertLineups(listOf(lineup)).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(lineupRepository).insertLineups(listOf(lineup))
    }

    @Test
    fun shouldGetCompleteRosterForTheCurrentTeam() {
        Mockito.`when`(playerRepository.getPlayersByTeamId(mainTeam.id))
            .thenReturn(Single.just(emptyList()))

        val observer = TestObserver<com.telen.easylineup.domain.model.TeamRosterSummary>()
        interactor.getCompleteRoster().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertTrue(observer.values().first().players.isEmpty())
    }

    @Test
    fun shouldGetRosterForALineupOfTheCurrentTeam() {
        Mockito.`when`(playerRepository.getPlayersNumberOverlay(lineup.id))
            .thenReturn(Single.just(emptyList()))
        Mockito.`when`(lineupRepository.getLineupByIdSingle(lineup.id))
            .thenReturn(Single.just(lineup))
        Mockito.`when`(playerRepository.getPlayersByTeamId(mainTeam.id))
            .thenReturn(Single.just(emptyList()))

        val observer = TestObserver<com.telen.easylineup.domain.model.TeamRosterSummary>()
        interactor.getRoster(lineup.id).subscribe(observer)
        observer.await()

        observer.assertComplete()
    }

    @Test
    fun shouldUpdateRoster() {
        val status = com.telen.easylineup.domain.model.RosterPlayerStatus(
            Player(id = 1L, teamId = mainTeam.id, name = "Toto", shirtNumber = 1, licenseNumber = 1L),
            status = true,
            playerNumberOverlay = null
        )
        Mockito.`when`(lineupRepository.getLineupByIdSingle(lineup.id))
            .thenReturn(Single.just(lineup))
        Mockito.`when`(lineupRepository.updateLineup(any()))
            .thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.updateRoster(lineup.id, listOf(status)).subscribe(observer)
        observer.await()

        observer.assertComplete()
    }

    @Test
    fun shouldSaveLineupForTheCurrentTeam() {
        Mockito.`when`(lineupRepository.insertLineup(any())).thenReturn(Single.just(99L))
        val rosterFilter = com.telen.easylineup.domain.model.TeamRosterSummary(
            com.telen.easylineup.domain.Constants.STATUS_ALL,
            emptyList()
        )
        val newLineup = Lineup(name = "Game 2", tournamentId = 5L, strategy = TeamStrategy.STANDARD.id)

        val observer = TestObserver<Lineup>()
        interactor.saveLineup(newLineup, rosterFilter).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(99L, observer.values().first().id)
    }

    @Test
    fun shouldFailToSaveLineupWhenNameIsEmpty() {
        val rosterFilter = com.telen.easylineup.domain.model.TeamRosterSummary(
            com.telen.easylineup.domain.Constants.STATUS_ALL,
            emptyList()
        )
        val invalidLineup = Lineup(name = "  ", tournamentId = 5L)

        val observer = TestObserver<Lineup>()
        interactor.saveLineup(invalidLineup, rosterFilter).subscribe(observer)
        observer.await()

        observer.assertError(LineupNameEmptyException::class.java)
    }

    @Test
    fun shouldDeleteLineup() {
        Mockito.`when`(lineupRepository.getLineupByIdSingle(lineup.id))
            .thenReturn(Single.just(lineup))
        Mockito.`when`(lineupRepository.deleteLineup(lineup)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.deleteLineup(lineup.id).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(lineupRepository).deleteLineup(lineup)
    }

    @Test
    fun shouldUpdateLineupMode() {
        val players = listOf(playerWithPosition(1L, FieldPosition.PITCHER.id))

        val observer = TestObserver<Void>()
        interactor.updateLineupMode(isEnabled = true, lineup = lineup, list = players)
            .subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(MODE_ENABLED, lineup.mode)
    }

    @Test
    fun shouldUpdateLineupAndPlayers() {
        val players = listOf(playerWithPosition(1L, FieldPosition.PITCHER.id))
        Mockito.`when`(playerFieldPositionRepository.insertPlayerFieldPosition(any()))
            .thenReturn(Single.just(1L))
        Mockito.`when`(lineupRepository.updateLineup(lineup)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.updateLineupAndPlayers(lineup, players).subscribe(observer)
        observer.await()

        observer.assertComplete()
    }

    @Test
    fun shouldUpdateLineup() {
        Mockito.`when`(lineupRepository.updateLineup(lineup)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.updateLineup(lineup).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(lineupRepository).updateLineup(lineup)
    }

    @Test
    fun shouldObserveLineupByIdDelegateToRepository() {
        val liveData = androidx.lifecycle.MutableLiveData<Lineup>()
        Mockito.`when`(lineupRepository.getLineupById(lineup.id)).thenReturn(liveData)

        assertSame(liveData, interactor.observeLineupById(lineup.id))
    }

    @Test
    fun shouldGetLineupByIdDelegateToRepository() {
        Mockito.`when`(lineupRepository.getLineupByIdSingle(lineup.id))
            .thenReturn(Single.just(lineup))

        val observer = TestObserver<Lineup>()
        interactor.getLineupById(lineup.id).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(lineup, observer.values().first())
    }

    @Test
    fun shouldObserveTeamPlayersAndMaybePositionsForLineupDelegateToRepository() {
        val liveData = androidx.lifecycle.MutableLiveData<List<PlayerWithPosition>>()
        Mockito.`when`(playerRepository.getTeamPlayersAndMaybePositions(lineup.id))
            .thenReturn(liveData)

        assertSame(liveData, interactor.observeTeamPlayersAndMaybePositionsForLineup(lineup.id))
    }

    @Test
    fun shouldGetDpAndFlexFromPlayersInField() {
        val players = listOf(playerWithPosition(1L, FieldPosition.PITCHER.id))

        val observer = TestObserver<com.telen.easylineup.domain.model.DpAndFlexConfiguration>()
        interactor.getDpAndFlexFromPlayersInField(players).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(mainTeam.type, observer.values().first().teamType)
    }

    @Test
    fun shouldLinkDpAndFlex() {
        val dp = Player(id = 1L, teamId = mainTeam.id, name = "Dp", shirtNumber = 1, licenseNumber = 1L)
        val flex = Player(id = 2L, teamId = mainTeam.id, name = "Flex", shirtNumber = 2, licenseNumber = 2L)
        val players = listOf(
            playerWithPosition(dp.id, FieldPosition.SHORT_STOP.id),
            playerWithPosition(flex.id, FieldPosition.PITCHER.id)
        )

        val observer = TestObserver<Void>()
        interactor.linkDpAndFlex(dp, flex, lineup, players).subscribe(observer)
        observer.await()

        observer.assertComplete()
    }

    @Test
    fun shouldGetBatterStates() {
        val players = listOf(playerWithPosition(1L, FieldPosition.PITCHER.id))

        val observer = TestObserver<List<BatterState>>()
        interactor.getBatterStates(
            players = players,
            teamType = mainTeam.type,
            batterSize = TeamStrategy.STANDARD.batterSize,
            extraHitterSize = 0,
            lineupMode = MODE_DISABLED,
            isDebug = false,
            isEditable = true
        ).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(1, observer.values().first().size)
    }

    @Test
    fun shouldUpdatePlayersWithBatters() {
        val players = listOf(playerWithPosition(1L, FieldPosition.PITCHER.id))
        val batters = listOf(
            BatterState(
                playerId = 1L,
                playerFlag = 0,
                playerOrder = 3,
                playerName = "player1",
                playerNumber = "1",
                playerPosition = FieldPosition.PITCHER,
                playerPositionDesc = "",
                canShowPosition = true,
                canMove = true,
                canShowDescription = true,
                canShowOrder = true,
                applyBackground = false,
                isEditable = true
            )
        )

        val observer = TestObserver<Void>()
        interactor.updatePlayersWithBatters(players, batters).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(3, players.first().order)
    }

    @Test
    fun shouldGetNotSelectedPlayersFromList() {
        val list = listOf(playerWithPosition(1L, -1))
        val teamPlayer = Player(id = 1L, teamId = mainTeam.id, name = "player1", shirtNumber = 1, licenseNumber = 1L)
        Mockito.`when`(playerRepository.getPlayersNumberOverlay(lineup.id))
            .thenReturn(Single.just(emptyList()))
        Mockito.`when`(lineupRepository.getLineupByIdSingle(lineup.id))
            .thenReturn(Single.just(lineup))
        Mockito.`when`(playerRepository.getPlayersByTeamId(mainTeam.id))
            .thenReturn(Single.just(listOf(teamPlayer)))

        val observer = TestObserver<List<PlayerWithPosition>>()
        interactor.getNotSelectedPlayersFromList(list, lineup).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(1, observer.values().first().size)
    }

    @Test
    fun shouldGetPlayersInFieldFromList() {
        val list = listOf(
            playerWithPosition(1L, FieldPosition.PITCHER.id),
            playerWithPosition(2L, -1)
        )

        val observer = TestObserver<List<PlayerWithPosition>>()
        interactor.getPlayersInFieldFromList(list).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(1, observer.values().first().size)
    }

    @Test
    fun shouldExposeAnErrorsSubject() {
        val observer = TestObserver<Any>()
        interactor.observeErrors().subscribe(observer)

        observer.assertNotComplete()
        observer.assertNoErrors()
    }
}

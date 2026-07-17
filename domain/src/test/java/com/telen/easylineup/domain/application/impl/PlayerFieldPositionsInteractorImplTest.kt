/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import com.telen.easylineup.domain.usecases.exceptions.FirstPositionEmptyException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
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
 * Characterization tests for [PlayerFieldPositionsInteractorImpl], written as a safety net
 * before merging the Interactor layer into the UseCase layer.
 */
@RunWith(MockitoJUnitRunner::class)
internal class PlayerFieldPositionsInteractorImplTest : BaseInteractorTest() {

    @Mock
    lateinit var playerFieldPositionRepository: PlayerFieldPositionRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    lateinit var interactor: PlayerFieldPositionsInteractorImpl

    private val mainTeam = Team(id = 1L, name = "Panthers", type = 0, main = true)
    private val lineup = Lineup(id = 10L, teamId = 1L, strategy = com.telen.easylineup.domain.model.TeamStrategy.STANDARD.id)
    private val player = Player(id = 42L, teamId = 1L, name = "Toto", shirtNumber = 7, licenseNumber = 1L)

    private fun playerWithPosition(playerId: Long, position: Int): PlayerWithPosition {
        return PlayerWithPosition(
            playerName = "player$playerId",
            shirtNumber = playerId.toInt(),
            licenseNumber = playerId,
            teamId = 1L,
            image = null,
            position = position,
            x = 0f,
            y = 0f,
            flags = 0,
            order = 1,
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

        loadKoinModules(
            module {
                single { playerFieldPositionRepository }
                single { GetTeam(teamRepository) }
                single { AssignPlayerFieldPosition() }
                single { DeletePlayerFieldPosition() }
                single { SwitchPlayersPosition() }
            }
        )

        Mockito.`when`(teamRepository.getTeamsRx()).thenReturn(Single.just(listOf(mainTeam)))

        interactor = PlayerFieldPositionsInteractorImpl()
    }

    @Test
    fun shouldDelegateInsertPlayerFieldPositionsToRepository() {
        val positions = listOf(PlayerFieldPosition(id = 1L, playerId = 42L, lineupId = 10L))
        Mockito.`when`(playerFieldPositionRepository.insertPlayerFieldPositions(positions))
            .thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.insertPlayerFieldPositions(positions).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerFieldPositionRepository).insertPlayerFieldPositions(positions)
    }

    @Test
    fun shouldSavePlayerFieldPositionForTheCurrentTeam() {
        val list = listOf(playerWithPosition(player.id, FieldPosition.OLD_SUBSTITUTE.id))

        val observer = TestObserver<Void>()
        interactor.savePlayerFieldPosition(player, FieldPosition.PITCHER, lineup, list)
            .subscribe(observer)
        observer.await()

        observer.assertComplete()
        assert(list.first().position == FieldPosition.PITCHER.id)
    }

    @Test
    fun shouldFailToSavePlayerFieldPositionWhenPlayerIsNotInTheList() {
        val list = listOf(playerWithPosition(999L, FieldPosition.OLD_SUBSTITUTE.id))

        val observer = TestObserver<Void>()
        interactor.savePlayerFieldPosition(player, FieldPosition.PITCHER, lineup, list)
            .subscribe(observer)
        observer.await()

        observer.assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldDeletePlayerPosition() {
        val list = listOf(playerWithPosition(player.id, FieldPosition.PITCHER.id))

        val observer = TestObserver<Void>()
        interactor.deletePlayerPosition(player, list, lineup.mode, lineup.extraHitters)
            .subscribe(observer)
        observer.await()

        observer.assertComplete()
    }

    @Test
    fun shouldFailToDeletePlayerPositionWhenPlayerIsNotInTheList() {
        val list = listOf(playerWithPosition(999L, FieldPosition.PITCHER.id))

        val observer = TestObserver<Void>()
        interactor.deletePlayerPosition(player, list, lineup.mode, lineup.extraHitters)
            .subscribe(observer)
        observer.await()

        observer.assertError(NoSuchElementException::class.java)
    }

    @Test
    fun shouldSwitchPlayersPositionForTheCurrentTeam() {
        val list = listOf(
            playerWithPosition(1L, FieldPosition.PITCHER.id),
            playerWithPosition(2L, FieldPosition.CATCHER.id)
        )

        val observer = TestObserver<Void>()
        interactor.switchPlayersPosition(
            FieldPosition.PITCHER,
            FieldPosition.CATCHER,
            list,
            lineup
        ).subscribe(observer)
        observer.await()

        observer.assertComplete()
    }

    @Test
    fun shouldFailToSwitchPlayersPositionWhenFirstPositionIsEmpty() {
        val list = listOf(playerWithPosition(2L, FieldPosition.CATCHER.id))

        val observer = TestObserver<Void>()
        interactor.switchPlayersPosition(
            FieldPosition.PITCHER,
            FieldPosition.CATCHER,
            list,
            lineup
        ).subscribe(observer)
        observer.await()

        observer.assertError(FirstPositionEmptyException::class.java)
    }
}

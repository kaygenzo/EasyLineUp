/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import com.telen.easylineup.domain.testUseCaseHandler
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.utils.ValidatorUtils
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
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

/**
 * Characterization tests for [PlayersInteractorImpl], written as a safety net before
 * merging the Interactor layer into the UseCase layer.
 */
@RunWith(MockitoJUnitRunner::class)
internal class PlayersInteractorImplTest {

    @Mock
    lateinit var playerRepository: PlayerRepository

    @Mock
    lateinit var playerFieldPositionRepository: PlayerFieldPositionRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    lateinit var interactor: PlayersInteractorImpl

    private val mainTeam = Team(id = 1L, name = "Panthers", type = 0, main = true)
    private val player = Player(
        id = 1L,
        teamId = 1L,
        name = "Toto",
        shirtNumber = 7,
        licenseNumber = 12L,
        email = "toto@mail.com",
        phone = null
    )

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(teamRepository.getTeamsRx()).thenReturn(Single.just(listOf(mainTeam)))

        interactor = PlayersInteractorImpl(
            playersRepo = playerRepository,
            getPlayer = GetPlayer(playerRepository),
            deletePlayer = DeletePlayer(playerRepository),
            savePlayer = SavePlayer(playerRepository),
            getPlayerPositionsSummary = GetPositionsSummaryForPlayer(playerFieldPositionRepository),
            getPlayers = GetPlayers(playerRepository),
            getTeam = GetTeam(teamRepository),
            savePlayerNumberOverlay = SavePlayerNumberOverlay(playerRepository),
            getShirtNumberHistory = GetShirtNumberHistory(playerRepository),
            validatorUtils = ValidatorUtils(),
            useCaseHandler = testUseCaseHandler()
        )
    }

    @Test
    fun shouldDelegateInsertPlayersToRepository() {
        Mockito.`when`(playerRepository.insertPlayers(listOf(player)))
            .thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.insertPlayers(listOf(player)).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerRepository).insertPlayers(listOf(player))
    }

    @Test
    fun shouldGetPlayerDelegateToRepository() {
        val liveData = androidx.lifecycle.MutableLiveData<Player>()
        Mockito.`when`(playerRepository.getPlayerById(player.id)).thenReturn(liveData)

        assertSame(liveData, interactor.getPlayer(player.id))
    }

    @Test
    fun shouldGetPlayerPositionsSummary() {
        Mockito.`when`(playerFieldPositionRepository.getAllPositionsForPlayer(player.id))
            .thenReturn(Single.just(emptyList()))

        val observer = TestObserver<Map<*, *>>()
        interactor.getPlayerPositionsSummary(player.id).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertTrue(observer.values().first().isEmpty())
    }

    @Test
    fun shouldSavePlayerForTheCurrentTeam() {
        Mockito.`when`(playerRepository.insertPlayer(any())).thenReturn(Single.just(9L))

        val observer = TestObserver<Void>()
        interactor.savePlayer(
            playerId = null,
            name = "Titi",
            shirtNumber = 8,
            licenseNumber = 42L,
            imageUri = null,
            positions = 0,
            pitching = 0,
            batting = 0,
            email = null,
            phone = null,
            sex = 0
        ).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerRepository).insertPlayer(any())
    }

    @Test
    fun shouldFailToSavePlayerWhenNameIsEmpty() {
        val observer = TestObserver<Void>()
        val errorObserver = TestObserver<Any>()
        interactor.observeErrors().subscribe(errorObserver)

        interactor.savePlayer(
            playerId = null,
            name = "  ",
            shirtNumber = 8,
            licenseNumber = 42L,
            imageUri = null,
            positions = 0,
            pitching = 0,
            batting = 0,
            email = null,
            phone = null,
            sex = 0
        ).subscribe(observer)
        observer.await()

        observer.assertError(NameEmptyException::class.java)
        Mockito.verifyZeroInteractions(playerRepository)
    }

    @Test
    fun shouldFailToSavePlayerWhenEmailIsInvalid() {
        val observer = TestObserver<Void>()
        interactor.savePlayer(
            playerId = null,
            name = "Titi",
            shirtNumber = 8,
            licenseNumber = 42L,
            imageUri = null,
            positions = 0,
            pitching = 0,
            batting = 0,
            email = "not-an-email",
            phone = null,
            sex = 0
        ).subscribe(observer)
        observer.await()

        observer.assertError(InvalidEmailException::class.java)
    }

    @Test
    fun shouldDeletePlayer() {
        Mockito.`when`(playerRepository.getPlayerByIdAsSingle(player.id))
            .thenReturn(Single.just(player))
        Mockito.`when`(playerRepository.deletePlayer(player)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.deletePlayer(player.id).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerRepository).deletePlayer(player)
    }

    @Test
    fun shouldGetPlayersForTheCurrentTeam() {
        Mockito.`when`(playerRepository.getPlayersByTeamId(mainTeam.id))
            .thenReturn(Single.just(listOf(player)))

        val observer = TestObserver<List<Player>>()
        interactor.getPlayers().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf(player), observer.values().first())
    }

    @Test
    fun shouldObservePlayersDelegateToRepository() {
        val liveData = androidx.lifecycle.MutableLiveData<List<Player>>()
        Mockito.`when`(playerRepository.observePlayers(mainTeam.id)).thenReturn(liveData)

        assertSame(liveData, interactor.observePlayers(mainTeam.id))
    }

    @Test
    fun shouldSaveOrUpdatePlayerNumberOverlays() {
        val overlay = PlayerNumberOverlay(id = 0L, lineupId = 10L, playerId = player.id, number = 8)
        val item = RosterItem(player, selected = true, playerNumberOverlay = overlay)

        Mockito.`when`(playerRepository.deletePlayerNumberOverlays(emptyList()))
            .thenReturn(Completable.complete())
        Mockito.`when`(playerRepository.updatePlayerNumberOverlays(emptyList()))
            .thenReturn(Completable.complete())
        Mockito.`when`(playerRepository.createPlayerNumberOverlays(listOf(overlay)))
            .thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.saveOrUpdatePlayerNumberOverlays(listOf(item)).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerRepository).createPlayerNumberOverlays(listOf(overlay))
    }

    @Test
    fun shouldGetShirtNumberHistoryForTheCurrentTeam() {
        Mockito.`when`(playerRepository.getShirtNumberFromPlayers(mainTeam.id, 8))
            .thenReturn(Single.just(emptyList()))
        Mockito.`when`(playerRepository.getShirtNumberFromNumberOverlays(mainTeam.id, 8))
            .thenReturn(Single.just(emptyList()))

        val observer = TestObserver<List<ShirtNumberEntry>>()
        interactor.getShirtNumberHistory(8).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertTrue(observer.values().first().isEmpty())
    }

    @Test
    fun shouldDelegateInsertPlayerNumberOverlaysToRepository() {
        val overlay = PlayerNumberOverlay(id = 0L, lineupId = 10L, playerId = player.id, number = 8)
        Mockito.`when`(playerRepository.createPlayerNumberOverlays(listOf(overlay)))
            .thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.insertPlayerNumberOverlays(listOf(overlay)).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(playerRepository).createPlayerNumberOverlays(listOf(overlay))
    }

    // getTeamEmails()/getTeamPhones() are intentionally not covered here: they call
    // android.text.TextUtils.isEmpty(), which throws "not mocked" in a plain JVM unit test
    // (no Robolectric in this module). This is itself a symptom of the Android leakage into
    // `domain` flagged in the architecture review - fixing that (Phase 3) will make these
    // testable with plain Kotlin (`isNullOrEmpty()`).

    @Test
    fun shouldObservePlayerNumberOverlaysDelegateToRepository() {
        val liveData = androidx.lifecycle.MutableLiveData<List<PlayerNumberOverlay>>()
        Mockito.`when`(playerRepository.observePlayersNumberOverlay(10L)).thenReturn(liveData)

        assertSame(liveData, interactor.observePlayerNumberOverlays(10L))
    }

    @Test
    fun shouldExposeAnErrorsSubject() {
        val observer = TestObserver<Any>()
        interactor.observeErrors().subscribe(observer)

        observer.assertNotComplete()
        observer.assertNoErrors()
    }
}

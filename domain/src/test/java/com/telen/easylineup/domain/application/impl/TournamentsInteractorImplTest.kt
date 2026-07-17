/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineupsUseCase
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.GetTournamentStatsForPositionTable
import com.telen.easylineup.domain.usecases.GetTournaments
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.exceptions.MapApiKeyNotFoundException
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
 * Characterization tests for [TournamentsInteractorImpl], written as a safety net before
 * merging the Interactor layer into the UseCase layer.
 */
@RunWith(MockitoJUnitRunner::class)
internal class TournamentsInteractorImplTest : BaseInteractorTest() {

    @Mock
    lateinit var tournamentRepository: TournamentRepository

    @Mock
    lateinit var lineupRepository: LineupRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var geocoder: Geocoder

    lateinit var interactor: TournamentsInteractorImpl

    private val mainTeam = Team(id = 1L, name = "Panthers", type = 0, main = true)
    private val tournament = Tournament(
        id = 5L,
        name = "Summer cup",
        createdAt = 1000L,
        startTime = 2000L,
        endTime = 3000L,
        address = "1 rue de la Paix, Paris"
    )

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        loadKoinModules(
            module {
                single { tournamentRepository }
                single { GetTeam(teamRepository) }
                single { DeleteTournamentLineups(lineupRepository) }
                single { GetAllTournamentsWithLineupsUseCase(lineupRepository) }
                single { GetTournaments(tournamentRepository) }
                single { GetTournamentStatsForPositionTable(context, lineupRepository) }
                single { SaveTournament(tournamentRepository) }
                single { GetTournamentMapLink(geocoder) }
            }
        )

        Mockito.`when`(teamRepository.getTeamsRx()).thenReturn(Single.just(listOf(mainTeam)))

        interactor = TournamentsInteractorImpl()
    }

    @Test
    fun shouldGetTournaments() {
        Mockito.`when`(tournamentRepository.getTournaments())
            .thenReturn(Single.just(listOf(tournament)))

        val observer = TestObserver<List<Tournament>>()
        interactor.getTournaments().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf(tournament), observer.values().first())
    }

    @Test
    fun shouldObserveTournamentsDelegateToRepository() {
        val liveData = androidx.lifecycle.MutableLiveData<List<Tournament>>()
        Mockito.`when`(tournamentRepository.observeTournaments()).thenReturn(liveData)

        assertSame(liveData, interactor.observeTournaments())
    }

    @Test
    fun shouldDelegateInsertTournamentsToRepository() {
        Mockito.`when`(tournamentRepository.insertTournaments(listOf(tournament)))
            .thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.insertTournaments(listOf(tournament)).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(tournamentRepository).insertTournaments(listOf(tournament))
    }

    @Test
    fun shouldDeleteTournamentAndItsLineupsForTheCurrentTeam() {
        Mockito.`when`(lineupRepository.getLineupsForTournamentRx(tournament.id, mainTeam.id))
            .thenReturn(Single.just(emptyList()))
        Mockito.`when`(lineupRepository.deleteLineups(emptyList())).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.deleteTournament(tournament).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(lineupRepository).getLineupsForTournamentRx(tournament.id, mainTeam.id)
    }

    @Test
    fun shouldGetCategorizedLineupsForTheCurrentTeam() {
        Mockito.`when`(lineupRepository.getAllTournamentsWithLineups("summer", mainTeam.id))
            .thenReturn(Single.just(emptyList()))

        val observer = TestObserver<List<Pair<Tournament, List<com.telen.easylineup.domain.model.Lineup>>>>()
        interactor.getCategorizedLineups("summer").subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertTrue(observer.values().first().isEmpty())
    }

    @Test
    fun shouldPropagateRepositoryErrorWhenComputingPositionStats() {
        val error = RuntimeException("db error")
        Mockito.`when`(
            lineupRepository.getAllPlayerPositionsForTournament(tournament.id, mainTeam.id)
        ).thenReturn(Single.error(error))

        val observer = TestObserver<Any>()
        interactor.getPlayersPositionForTournament(tournament, TeamStrategy.STANDARD)
            .subscribe(observer)
        observer.await()

        observer.assertError(error)
        Mockito.verify(lineupRepository)
            .getAllPlayerPositionsForTournament(tournament.id, mainTeam.id)
    }

    @Test
    fun shouldSaveNewTournament() {
        val newTournament = Tournament(name = "Winter cup", createdAt = 0L, startTime = 0L, endTime = 0L)
        Mockito.`when`(tournamentRepository.getTournamentByName("Winter cup"))
            .thenReturn(Single.error(NoSuchElementException()))
        Mockito.`when`(tournamentRepository.insertTournament(newTournament))
            .thenReturn(Single.just(7L))

        val observer = TestObserver<Void>()
        interactor.saveTournament(newTournament).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(tournamentRepository).insertTournament(newTournament)
    }

    @Test
    fun shouldGetTournamentMapInfoWhenAddressResolves() {
        val address = Mockito.mock(Address::class.java)
        Mockito.`when`(address.latitude).thenReturn(48.8566)
        Mockito.`when`(address.longitude).thenReturn(2.3522)
        Mockito.`when`(geocoder.getFromLocationName(tournament.address!!, 1))
            .thenReturn(listOf(address))

        val observer = TestObserver<com.telen.easylineup.domain.model.MapInfo>()
        interactor.getTournamentMapInfo(tournament, "some-api-key", 300, 200).subscribe(observer)
        observer.await()

        observer.assertComplete()
        val mapInfo = observer.values().first()
        assertEquals(48.8566, mapInfo.location!!.latitude, 0.0001)
        assertEquals(2.3522, mapInfo.location!!.longitude, 0.0001)
    }

    @Test
    fun shouldFailToGetTournamentMapInfoWhenApiKeyIsMissing() {
        val observer = TestObserver<com.telen.easylineup.domain.model.MapInfo>()
        interactor.getTournamentMapInfo(tournament, null, 300, 200).subscribe(observer)
        observer.await()

        observer.assertError(MapApiKeyNotFoundException::class.java)
    }
}

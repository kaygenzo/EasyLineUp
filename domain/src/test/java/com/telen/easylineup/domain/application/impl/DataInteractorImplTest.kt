/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import android.content.Context
import com.telen.easylineup.domain.application.LineupsInteractor
import com.telen.easylineup.domain.application.PlayerFieldPositionsInteractor
import com.telen.easylineup.domain.application.TournamentsInteractor
import com.telen.easylineup.domain.mock.DatabaseMockProvider
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.testUseCaseHandler
import com.telen.easylineup.domain.usecases.CheckHashData
import com.telen.easylineup.domain.usecases.CreateDashboardTiles
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.domain.usecases.InsertPlayerNumberOverlays
import com.telen.easylineup.domain.usecases.InsertPlayers
import com.telen.easylineup.domain.usecases.InsertTeam
import com.telen.easylineup.domain.usecases.SaveDashboardTiles
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

/**
 * Characterization tests for [DataInteractorImpl], written as a safety net before merging
 * the Interactor layer into the UseCase layer.
 *
 * `exportData()`, `generateMockedData()` and `getDashboardConfigurations()` are intentionally
 * NOT covered here: they perform real Android file I/O (DocumentFile, ContentResolver, Gson,
 * assets) that can only be exercised with Robolectric, which this module doesn't use. This is
 * itself another symptom of the Android leakage into `domain` flagged in the architecture
 * review.
 */
@RunWith(MockitoJUnitRunner::class)
internal class DataInteractorImplTest {

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var tournamentRepository: TournamentRepository

    @Mock
    lateinit var playerRepository: PlayerRepository

    @Mock
    lateinit var lineupRepository: LineupRepository

    @Mock
    lateinit var playerFieldPositionRepository: PlayerFieldPositionRepository

    @Mock
    lateinit var tilesRepository: TilesRepository

    @Mock
    lateinit var context: Context

    lateinit var interactor: DataInteractorImpl

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        // Not exercised by the tests below - just need a valid graph to construct the interactor.
        val databaseMockProvider = DatabaseMockProvider(
            insertTeamUseCase = InsertTeam(teamRepository),
            insertPlayersUseCase = InsertPlayers(playerRepository),
            insertPlayerNumberOverlaysUseCase = InsertPlayerNumberOverlays(playerRepository),
            lineupsInteractor = Mockito.mock(LineupsInteractor::class.java),
            playerFieldPositionsInteractor = Mockito.mock(PlayerFieldPositionsInteractor::class.java),
            tournamentsInteractor = Mockito.mock(TournamentsInteractor::class.java)
        )

        interactor = DataInteractorImpl(
            context = context,
            getTeam = GetTeam(teamRepository),
            getDashboardTiles = GetDashboardTiles(
                playerRepository,
                lineupRepository,
                playerFieldPositionRepository,
                tilesRepository
            ),
            updateTiles = SaveDashboardTiles(tilesRepository),
            createTiles = CreateDashboardTiles(tilesRepository),
            deleteAllData = DeleteAllData(teamRepository, tournamentRepository),
            checkHash = CheckHashData(
                teamRepository,
                playerRepository,
                tournamentRepository,
                lineupRepository,
                playerFieldPositionRepository
            ),
            importer = ImportData(
                teamRepository,
                playerRepository,
                tournamentRepository,
                lineupRepository,
                playerFieldPositionRepository
            ),
            exportData = ExportData(
                teamRepository,
                playerRepository,
                tournamentRepository,
                lineupRepository,
                playerFieldPositionRepository
            ),
            databaseMockProvider = databaseMockProvider,
            useCaseHandler = testUseCaseHandler()
        )
    }

    @Test
    fun shouldImportEmptyExportWithoutTouchingRepositories() {
        val emptyExport = ExportBase(teams = emptyList())

        val observer = TestObserver<Void>()
        interactor.importData(emptyExport, updateIfExists = false).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verifyZeroInteractions(teamRepository, playerRepository, tournamentRepository)
    }

    @Test
    fun shouldDeleteAllData() {
        val team = Team(id = 1L, name = "Panthers", main = true)
        Mockito.`when`(tournamentRepository.getTournaments()).thenReturn(Single.just(emptyList()))
        Mockito.`when`(tournamentRepository.deleteTournaments(emptyList()))
            .thenReturn(Completable.complete())
        Mockito.`when`(teamRepository.getTeamsRx()).thenReturn(Single.just(listOf(team)))
        Mockito.`when`(teamRepository.deleteTeams(listOf(team))).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.deleteAllData().subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(teamRepository).deleteTeams(listOf(team))
    }

    @Test
    fun shouldUpdateDashboardConfiguration() {
        val tiles = listOf(DashboardTile(id = 1L, position = 0, type = 1))
        Mockito.`when`(tilesRepository.updateTiles(tiles)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.updateDashboardConfiguration(tiles).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(tilesRepository).updateTiles(tiles)
    }

    @Test
    fun shouldExposeAnErrorsSubject() {
        val observer = TestObserver<Any>()
        interactor.observeErrors().subscribe(observer)

        observer.assertNotComplete()
        observer.assertNoErrors()
    }

    // isDigitsOnly() is intentionally not covered here either: androidx.core.text.isDigitsOnly()
    // delegates to android.text.TextUtils.isDigitsOnly(), which throws "not mocked" in a plain
    // JVM unit test - same root cause as the getTeamEmails()/getTeamPhones() gap noted in
    // PlayersInteractorImplTest.

    @Test
    fun shouldValidateBlankValues() {
        assertTrue(interactor.isBlank("   "))
        assertFalse(interactor.isBlank("not blank"))
    }
}

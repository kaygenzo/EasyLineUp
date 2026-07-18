/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.domain.usecases.CreateDashboardTiles
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTeamEmails
import com.telen.easylineup.domain.usecases.GetTeamPhones
import com.telen.easylineup.domain.usecases.ObserveTeams
import com.telen.easylineup.domain.usecases.SaveDashboardTiles
import com.telen.easylineup.testSchedulersProvider
import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

/**
 * Characterization tests for [DashboardViewModel], demonstrating a pattern for testing the
 * `app` module ViewModels that resolve their UseCases through Koin (`by inject()`). The
 * remaining ViewModels listed in the refactor plan can follow the same shape: construct real
 * UseCases with mocked repositories, register them in a Koin test module, then exercise the
 * ViewModel's public API.
 */
@RunWith(MockitoJUnitRunner::class)
internal class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var teamRepository: TeamRepository

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

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        // lenient: each test only exercises one of these sub-ports
        Mockito.lenient().`when`(teamRepository.getTeamsRx())
            .thenReturn(Single.just(listOf(Team(id = 1L, name = "Panthers", main = true))))

        val schedulersProvider = testSchedulersProvider()
        val getTeam = GetTeam(teamRepository, schedulersProvider)
        val getPlayers = GetPlayers(playerRepository, getTeam, schedulersProvider)

        startKoin {
            modules(
                module {
                    single { ObserveTeams(teamRepository) }
                    single {
                        GetDashboardTiles(
                            playerRepository,
                            lineupRepository,
                            playerFieldPositionRepository,
                            tilesRepository,
                            getTeam,
                            CreateDashboardTiles(tilesRepository, schedulersProvider),
                            schedulersProvider
                        )
                    }
                    single { SaveDashboardTiles(tilesRepository, schedulersProvider) }
                    single { GetShirtNumberHistory(playerRepository, getTeam, schedulersProvider) }
                    single { GetTeamEmails(getPlayers, schedulersProvider) }
                    single { GetTeamPhones(getPlayers, schedulersProvider) }
                    single { SharedPreferencesHelper(context) }
                }
            )
        }

        viewModel = DashboardViewModel()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun shouldRegisterTilesLiveDataAndSwitchToDashboardConfigurationsWhenTeamsChange() {
        val teamsLiveData = MutableLiveData<List<Team>>()
        Mockito.`when`(teamRepository.getTeams()).thenReturn(teamsLiveData)
        val tile = DashboardTile(id = 1L, position = 0, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepository.getTiles()).thenReturn(Single.just(listOf(tile)))
        Mockito.`when`(playerRepository.getPlayersByTeamId(1L)).thenReturn(Single.just(emptyList()))

        val observedValues = mutableListOf<List<DashboardTile>>()
        viewModel.registerTilesLiveData().observeForever { observedValues.add(it) }

        teamsLiveData.value = emptyList()

        assertEquals(1, observedValues.size)
        assertEquals(1, observedValues.first().size)
    }

    @Test
    fun shouldShowReportIssueButtonOnlyOnceThenDisableTheFeature() {
        Mockito.`when`(
            context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, Context.MODE_PRIVATE)
        ).thenReturn(sharedPreferences)
        Mockito.`when`(
            sharedPreferences.getBoolean(
                Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON,
                true
            )
        ).thenReturn(true)
        val editor = Mockito.mock(SharedPreferences.Editor::class.java)
        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.`when`(editor.putBoolean(Mockito.anyString(), Mockito.anyBoolean()))
            .thenReturn(editor)

        val observer = TestObserver<Boolean>()
        viewModel.showNewReportIssueButtonFeature().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertTrue(observer.values().first())
        Mockito.verify(editor)
            .putBoolean(Constants.PREF_FEATURE_SHOW_REPORT_ISSUE_BUTTON, false)
    }

    @Test
    fun shouldSaveTilesDelegateToUseCase() {
        val tiles = listOf(DashboardTile(id = 1L, position = 0, type = 1))
        Mockito.`when`(tilesRepository.updateTiles(tiles))
            .thenReturn(io.reactivex.rxjava3.core.Completable.complete())

        val observer = TestObserver<Void>()
        viewModel.saveTiles(tiles).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(tilesRepository).updateTiles(tiles)
    }

    @Test
    fun shouldGetShirtNumberHistoryDelegateToUseCase() {
        val overlayEntry = ShirtNumberEntry(
            number = 8,
            playerName = "Toto",
            playerId = 1L,
            eventTime = 0L,
            createdAt = 0L,
            lineupId = 1L,
            lineupName = "Game 1"
        )
        Mockito.`when`(playerRepository.getShirtNumberFromPlayers(1L, 8))
            .thenReturn(Single.just(emptyList()))
        Mockito.`when`(playerRepository.getShirtNumberFromNumberOverlays(1L, 8))
            .thenReturn(Single.just(listOf(overlayEntry)))

        val observer = TestObserver<List<ShirtNumberEntry>>()
        viewModel.getShirtNumberHistory(8).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf(overlayEntry), observer.values().first())
    }

    @Test
    fun shouldGetEmailsDelegateToUseCase() {
        val withEmail = Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L, email = "a@mail.com")
        Mockito.`when`(playerRepository.getPlayersByTeamId(1L))
            .thenReturn(Single.just(listOf(withEmail)))

        val observer = TestObserver<List<String>>()
        viewModel.getEmails().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf("a@mail.com"), observer.values().first())
    }

    @Test
    fun shouldGetPhonesDelegateToUseCase() {
        val withPhone = Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L, phone = "0102030405")
        Mockito.`when`(playerRepository.getPlayersByTeamId(1L))
            .thenReturn(Single.just(listOf(withPhone)))

        val observer = TestObserver<List<String>>()
        viewModel.getPhones().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf("0102030405"), observer.values().first())
    }
}

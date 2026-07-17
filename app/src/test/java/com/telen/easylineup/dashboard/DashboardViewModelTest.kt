/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.application.DataInteractor
import com.telen.easylineup.domain.application.PlayersInteractor
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.ObserveTeams
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
 * `app` module ViewModels that resolve their [ApplicationInteractor] through Koin
 * (`by inject()`). The remaining ViewModels listed in the refactor plan can follow the same
 * shape: mock the interactor sub-ports, register them in a Koin test module, then exercise the
 * ViewModel's public API.
 */
@RunWith(MockitoJUnitRunner::class)
internal class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var applicationInteractor: ApplicationInteractor

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var dataInteractor: DataInteractor

    @Mock
    lateinit var playersInteractor: PlayersInteractor

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        // lenient: each test only exercises one of these sub-ports
        Mockito.lenient().`when`(applicationInteractor.data()).thenReturn(dataInteractor)
        Mockito.lenient().`when`(applicationInteractor.players()).thenReturn(playersInteractor)

        startKoin {
            modules(
                module {
                    single { applicationInteractor }
                    single { ObserveTeams(teamRepository) }
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
        val tilesLiveData = MutableLiveData<List<DashboardTile>>()
        Mockito.`when`(teamRepository.getTeams()).thenReturn(teamsLiveData)
        Mockito.`when`(dataInteractor.getDashboardConfigurations()).thenReturn(tilesLiveData)

        val observedValues = mutableListOf<List<DashboardTile>>()
        viewModel.registerTilesLiveData().observeForever { observedValues.add(it) }

        teamsLiveData.value = emptyList()
        val tiles = listOf(DashboardTile(id = 1L, position = 0, type = 1))
        tilesLiveData.value = tiles

        assertEquals(listOf(tiles), observedValues)
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
    fun shouldSaveTilesDelegateToDataInteractor() {
        val tiles = listOf(DashboardTile(id = 1L, position = 0, type = 1))
        Mockito.`when`(dataInteractor.updateDashboardConfiguration(tiles))
            .thenReturn(io.reactivex.rxjava3.core.Completable.complete())

        val observer = TestObserver<Void>()
        viewModel.saveTiles(tiles).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(dataInteractor).updateDashboardConfiguration(tiles)
    }

    @Test
    fun shouldGetShirtNumberHistoryDelegateToPlayersInteractor() {
        val history = listOf(
            ShirtNumberEntry(
                number = 8,
                playerName = "Toto",
                playerId = 1L,
                eventTime = 0L,
                createdAt = 0L,
                lineupId = 1L,
                lineupName = "Game 1"
            )
        )
        Mockito.`when`(playersInteractor.getShirtNumberHistory(8)).thenReturn(Single.just(history))

        val observer = TestObserver<List<ShirtNumberEntry>>()
        viewModel.getShirtNumberHistory(8).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(history, observer.values().first())
    }

    @Test
    fun shouldGetEmailsDelegateToPlayersInteractor() {
        Mockito.`when`(playersInteractor.getTeamEmails()).thenReturn(Single.just(listOf("a@mail.com")))

        val observer = TestObserver<List<String>>()
        viewModel.getEmails().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf("a@mail.com"), observer.values().first())
    }

    @Test
    fun shouldGetPhonesDelegateToPlayersInteractor() {
        Mockito.`when`(playersInteractor.getTeamPhones()).thenReturn(Single.just(listOf("0102030405")))

        val observer = TestObserver<List<String>>()
        viewModel.getPhones().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf("0102030405"), observer.values().first())
    }
}

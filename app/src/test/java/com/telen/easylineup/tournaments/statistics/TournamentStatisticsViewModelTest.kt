/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile
import com.telen.easylineup.domain.model.PlayingTimeSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetPlayingTimeSummary
import com.telen.easylineup.domain.usecases.GetTournamentPlayerStatsWithProfiles
import com.telen.easylineup.domain.usecases.IsPlayingTimeUnderused
import com.telen.easylineup.domain.usecases.SortPlayersByPlayingTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [TournamentStatisticsViewModel]: its use cases are mocked directly (each
 * already has its own domain-level test suite), so this only verifies the ViewModel's own
 * orchestration - merging stats/profiles/summary into [TournamentStatisticsUiState].
 */
@RunWith(MockitoJUnitRunner::class)
internal class TournamentStatisticsViewModelTest {
    @Mock
    lateinit var getTournamentPlayerStatsWithProfiles: GetTournamentPlayerStatsWithProfiles

    @Mock
    lateinit var sortPlayersByPlayingTime: SortPlayersByPlayingTime

    @Mock
    lateinit var getPlayingTimeSummary: GetPlayingTimeSummary

    @Mock
    lateinit var isPlayingTimeUnderused: IsPlayingTimeUnderused

    lateinit var viewModel: TournamentStatisticsViewModel

    private val tournament = Tournament(
        id = 5L,
        name = "Summer Cup",
        createdAt = 0,
        startTime = 0,
        endTime = 0
    )

    private fun profile(id: Long, totalGames: Int) = PlayerTournamentStatsProfile(
        playerId = id,
        playerName = "p$id",
        shirtNumber = id.toInt(),
        image = null,
        totalGames = totalGames,
        positionCounts = mapOf(FieldPosition.PITCHER to totalGames)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = TournamentStatisticsViewModel(
            getTournamentPlayerStatsWithProfiles,
            sortPlayersByPlayingTime,
            getPlayingTimeSummary,
            isPlayingTimeUnderused
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldSetLoadingAndTeamNameBeforeTheResultComesBack() = runTest {
        runBlocking {
            Mockito.`when`(getTournamentPlayerStatsWithProfiles(tournament))
                .thenReturn(Result.success(emptyList()))
        }
        Mockito.`when`(sortPlayersByPlayingTime(emptyList())).thenReturn(emptyList())
        Mockito.`when`(getPlayingTimeSummary(emptyList()))
            .thenReturn(PlayingTimeSummary(0f, 0, 0))

        viewModel.load(tournament.id, tournament.name, TeamType.BASEBALL.id)

        assertEquals(tournament.name, viewModel.uiState.value.tournamentName)
        assertEquals(TeamType.BASEBALL, viewModel.uiState.value.teamType)
    }

    @Test
    fun shouldPopulatePlayerStatsSortedAndFlaggedOnSuccess() = runTest {
        val profiles = listOf(profile(1L, 10), profile(2L, 2))
        val summary = PlayingTimeSummary(average = 6f, lowest = 2, highest = 10)

        runBlocking {
            Mockito.`when`(getTournamentPlayerStatsWithProfiles(tournament))
                .thenReturn(Result.success(profiles))
        }
        Mockito.`when`(getPlayingTimeSummary(profiles)).thenReturn(summary)
        Mockito.`when`(sortPlayersByPlayingTime(profiles)).thenReturn(profiles)
        Mockito.`when`(isPlayingTimeUnderused(10, 6f)).thenReturn(false)
        Mockito.`when`(isPlayingTimeUnderused(2, 6f)).thenReturn(true)

        viewModel.load(tournament.id, tournament.name, TeamType.BASEBALL.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(summary, state.playingTimeSummary)
        assertEquals(2, state.playerStats.size)
        assertEquals(1L, state.playerStats[0].playerId)
        assertFalse(state.playerStats[0].isUnderused)
        assertEquals(2L, state.playerStats[1].playerId)
        assertTrue(state.playerStats[1].isUnderused)
    }

    @Test
    fun shouldStopLoadingWithoutPlayerStatsOnFailure() = runTest {
        val exception = Exception("db error")
        runBlocking {
            Mockito.`when`(getTournamentPlayerStatsWithProfiles(tournament))
                .thenReturn(Result.failure(exception))
        }

        viewModel.load(tournament.id, tournament.name, TeamType.BASEBALL.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.loadFailed)
        assertTrue(state.playerStats.isEmpty())
    }

    @Test
    fun shouldReadTheSameTournamentAgainOnRetry() = runTest {
        runBlocking {
            Mockito.`when`(getTournamentPlayerStatsWithProfiles(tournament))
                .thenReturn(Result.failure(Exception("db error")))
                .thenReturn(Result.success(emptyList()))
        }
        Mockito.`when`(sortPlayersByPlayingTime(emptyList())).thenReturn(emptyList())
        Mockito.`when`(getPlayingTimeSummary(emptyList()))
            .thenReturn(PlayingTimeSummary(0f, 0, 0))
        viewModel.load(tournament.id, tournament.name, TeamType.BASEBALL.id)
        advanceUntilIdle()

        viewModel.onRetryLoad()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loadFailed)
        assertFalse(state.isLoading)
        assertEquals(tournament.name, state.tournamentName)
    }
}

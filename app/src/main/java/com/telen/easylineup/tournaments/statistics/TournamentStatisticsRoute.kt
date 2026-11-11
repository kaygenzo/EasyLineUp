/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.compose.LoadFailedState
import org.koin.androidx.compose.koinViewModel

/** Replaces `TournamentStatisticsFragment`. */
@Composable
fun TournamentStatisticsRoute(
    navController: NavController,
    tournamentId: Long,
    tournamentName: String,
    teamTypeId: Int
) {
    val viewModel = koinViewModel<TournamentStatisticsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, tournamentId) {
        viewModel.load(tournamentId, tournamentName, teamTypeId)
    }

    if (uiState.loadFailed) {
        LoadFailedState(onRetry = viewModel::onRetryLoad)
        return
    }

    TournamentStatisticsScreen(
        state = uiState,
        onBackClick = { navController.navigateUp() }
    )
}

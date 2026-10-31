/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/** Replaces `TournamentStatisticsFragment`. */
@Composable
fun TournamentStatisticsRoute(
    navController: NavController,
    tournamentId: Long,
    tournamentName: String,
    teamTypeId: Int
) {
    val viewModel = koinViewModel<TournamentStatisticsViewModel>()
    var playerStats by remember { mutableStateOf<List<PlayerStatsRow>>(emptyList()) }

    LaunchedEffect(viewModel, tournamentId) {
        // Only `.id` is actually read by GetTournamentPlayerStats - the other fields are
        // unused placeholders, avoiding the need for a custom NavType for the full Tournament.
        val tournament = Tournament(
            id = tournamentId,
            name = tournamentName,
            createdAt = 0,
            startTime = 0,
            endTime = 0
        )
        viewModel.load(tournament)
            .onSuccess { playerStats = it }
            .onFailure { Timber.e(it) }
    }

    TournamentStatisticsScreen(
        tournamentName = tournamentName,
        playerStats = playerStats,
        teamType = TeamType.getTypeById(teamTypeId),
        onBackClick = { navController.navigateUp() }
    )
}

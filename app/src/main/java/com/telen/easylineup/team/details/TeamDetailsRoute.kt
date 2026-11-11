/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.details

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.team.TeamEvent
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.tournaments.list.LineupListViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import org.koin.androidx.compose.koinViewModel

/** The current team's details. */
@Composable
fun TeamDetailsRoute(navController: NavController) {
    val teamViewModel = koinViewModel<TeamViewModel>()
    val lineupListViewModel = koinViewModel<LineupListViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val teamUiState by teamViewModel.uiState.collectAsStateWithLifecycle()
    val lineupListUiState by lineupListViewModel.uiState.collectAsStateWithLifecycle()

    val editTeam = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { teamViewModel.loadTeam() }

    var showDeleteSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(teamViewModel) {
        teamViewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    TeamEvent.TeamDeleted -> navController.popBackStack()
                    TeamEvent.TeamDeleteFailed -> Toast.makeText(
                        context,
                        R.string.error_when_deleting_team,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    val lineupsBuilt = remember(lineupListUiState.tournamentItems) {
        lineupListUiState.tournamentItems.sumOf { it.lineups.count() }
    }

    TeamDetailsScreen(
        state = TeamDetailsState(
            teamName = teamUiState.teamName,
            teamType = teamUiState.teamType,
            teamImageUri = teamUiState.teamImageUri,
            rosterSize = teamUiState.players.size,
            lineupsBuilt = lineupsBuilt,
            tournamentsCount = lineupListUiState.tournamentItems.size,
            maleCount = teamUiState.players.count { it.sex == Sex.MALE.id },
            femaleCount = teamUiState.players.count { it.sex == Sex.FEMALE.id },
            unsetCount = teamUiState.players.count { it.sex == Sex.UNKNOWN.id },
            createdAt = teamUiState.teamCreatedAt
        ),
        onBackClick = { navController.navigateUp() },
        onEditClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_team_details_edit")
            teamViewModel.team?.let { team ->
                FirebaseAnalyticsUtils.startTutorial(context, false)
                val intent = Intent(context, TeamCreationActivity::class.java)
                intent.putExtra(Constants.EXTRA_TEAM, team)
                editTeam.launch(intent)
            }
        },
        onDeleteClick = { showDeleteSheet = true }
    )

    if (showDeleteSheet) {
        ConfirmSheet(
            title = stringResource(R.string.dialog_delete_team_title, teamUiState.teamName),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = { showDeleteSheet = false },
            onConfirm = {
                teamViewModel.team?.let { team ->
                    FirebaseAnalyticsUtils.onClick(context, "click_team_details_delete")
                    teamViewModel.onDeleteTeam(team)
                }
            }
        )
    }
}

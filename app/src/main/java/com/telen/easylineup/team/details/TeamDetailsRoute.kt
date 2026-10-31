/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.details

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.exceptions.NoTeamsLeftException
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.tournaments.list.LineupViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/** Replaces `TeamDetailsFragment`. */
@Composable
fun TeamDetailsRoute(navController: NavController) {
    val teamViewModel = koinViewModel<TeamViewModel>()
    val lineupViewModel = koinViewModel<LineupViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val editTeam = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { teamViewModel.loadTeam() }

    var teamName by remember { mutableStateOf("") }
    var teamType by remember { mutableStateOf(TeamType.UNKNOWN) }
    var teamImageUri by remember { mutableStateOf<Uri?>(null) }
    var rosterSize by remember { mutableStateOf(0) }
    var lineupsBuilt by remember { mutableStateOf(0) }
    var tournamentsCount by remember { mutableStateOf(0) }
    var maleCount by remember { mutableStateOf(0) }
    var femaleCount by remember { mutableStateOf(0) }
    var unsetCount by remember { mutableStateOf(0) }
    var createdAt by remember { mutableStateOf(0L) }
    var showDeleteSheet by remember { mutableStateOf(false) }

    LaunchedEffect(teamViewModel, lineupViewModel) {
        teamViewModel.observeCurrentTeamName()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(this)

        teamViewModel.observeCurrentTeamType()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamType = it }
            .launchIn(this)

        teamViewModel.observeCurrentTeamImage()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamImageUri = it }
            .launchIn(this)

        teamViewModel.observeCurrentTeamCreatedAt()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { createdAt = it }
            .launchIn(this)

        teamViewModel.observePlayers()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { players ->
                rosterSize = players.size
                maleCount = players.count { it.sex == Sex.MALE.id }
                femaleCount = players.count { it.sex == Sex.FEMALE.id }
                unsetCount = players.count { it.sex == Sex.UNKNOWN.id }
            }
            .launchIn(this)

        lineupViewModel.observeCategorizedLineups()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                tournamentsCount = it.size
                lineupsBuilt = it.sumOf { item -> item.lineups.count() }
            }
            .launchIn(this)
    }

    TeamDetailsScreen(
        state = TeamDetailsState(
            teamName = teamName,
            teamType = teamType,
            teamImageUri = teamImageUri,
            rosterSize = rosterSize,
            lineupsBuilt = lineupsBuilt,
            tournamentsCount = tournamentsCount,
            maleCount = maleCount,
            femaleCount = femaleCount,
            unsetCount = unsetCount,
            createdAt = createdAt
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
            title = stringResource(R.string.dialog_delete_team_title, teamName),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = { showDeleteSheet = false },
            onConfirm = {
                val team = teamViewModel.team
                if (team != null) {
                    coroutineScope.launch {
                        FirebaseAnalyticsUtils.onClick(context, "click_team_details_delete")
                        teamViewModel.deleteTeam(team)
                            .onSuccess { navController.popBackStack() }
                            .onFailure { throwable ->
                                // DeleteTeam always deletes before throwing: NoTeamsLeftException
                                // only means there was no other team left to promote to main, not
                                // that the delete was refused - same outcome as onSuccess.
                                if (throwable is NoTeamsLeftException) {
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.error_when_deleting_team,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                }
            }
        )
    }
}

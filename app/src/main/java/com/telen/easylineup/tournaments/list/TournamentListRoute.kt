/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.LoadFailedState
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.create.TournamentPickerSheet
import com.telen.easylineup.lineup.edition.EditLineupSheet
import com.telen.easylineup.lineup.edition.LineupEditionViewModel
import com.telen.easylineup.lineup.edition.LineupLoadFailed
import com.telen.easylineup.lineup.edition.LineupUpdateFailed
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.startActivityOrNotify
import org.koin.androidx.compose.koinViewModel

/** The tournaments and their lineups, the `navigation_lineups` root tab. */
@Composable
fun TournamentListRoute(navController: NavController) {
    val viewModel = koinViewModel<LineupListViewModel>()
    val teamViewModel = koinViewModel<TeamViewModel>()
    val editionViewModel = koinViewModel<LineupEditionViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val teamUiState by teamViewModel.uiState.collectAsStateWithLifecycle()
    val editionUiState by editionViewModel.uiState.collectAsStateWithLifecycle()

    var tournamentPendingDeletion by rememberSaveable { mutableStateOf<Tournament?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect {
                Toast.makeText(context, R.string.error_tournament_delete_failed, Toast.LENGTH_LONG)
                    .show()
            }
    }

    LaunchedEffect(editionViewModel) {
        editionViewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    LineupUpdateFailed -> Toast.makeText(
                        context,
                        R.string.error_lineup_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    LineupLoadFailed -> Toast.makeText(
                        context,
                        R.string.error_lineup_load_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    if (uiState.loadFailed) {
        LoadFailedState(onRetry = viewModel::onRetryLoad)
        return
    }

    TournamentListScreen(
        state = TournamentListState(
            teamName = teamUiState.teamName,
            teamType = uiState.teamType,
            items = uiState.tournamentItems,
            mapsByTournamentId = uiState.mapsByTournamentId,
            searchQuery = uiState.searchQuery
        ),
        onBackClick = { navController.navigateUp() },
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onCreateLineupClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_tournaments_create")
            navController.navigate(Routes.LineupCreation)
        },
        onLineupClick = { lineup ->
            FirebaseAnalyticsUtils.onClick(context, "click_tournaments_lineup_selected")
            navController.navigate(Routes.Lineup(lineupId = lineup.id, editable = false))
        },
        onEditLineupClick = { lineup -> editionViewModel.onEditLineup(lineup.id) },
        onStatsClick = { tournament ->
            FirebaseAnalyticsUtils.onClick(context, "click_tournaments_stats")
            navController.navigate(
                Routes.TournamentStatistics(
                    tournamentId = tournament.id,
                    tournamentName = tournament.name,
                    teamType = uiState.teamType.id
                )
            )
        },
        onDeleteClick = { tournamentPendingDeletion = it },
        onMapClick = { location ->
            val urlString = "geo:${location.latitude},${location.longitude}"
            context.startActivityOrNotify(Intent(Intent.ACTION_VIEW, urlString.toUri()))
        }
    )

    tournamentPendingDeletion?.let { tournament ->
        ConfirmSheet(
            title = stringResource(R.string.dialog_delete_tournament_title, tournament.name),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = { tournamentPendingDeletion = null },
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_tournaments_delete")
                viewModel.onDeleteTournament(tournament)
            }
        )
    }

    editionUiState.form?.let { form ->
        EditLineupSheet(
            name = form.name,
            nameError = stringResource(R.string.lineup_creation_error_name_empty)
                .takeIf { form.nameError },
            selectedTournament = form.tournament,
            tournamentError = stringResource(R.string.lineup_creation_error_tournament_empty)
                .takeIf { form.tournamentError },
            roster = form.roster,
            step = form.step,
            teamType = uiState.teamType,
            onDismiss = editionViewModel::onCancel,
            onNameChanged = editionViewModel::onNameChanged,
            onTournamentClick = editionViewModel::onTournamentClick,
            onRosterClick = editionViewModel::onRosterClick,
            onRosterBackClick = editionViewModel::onRosterBackClick,
            onPlayerToggled = editionViewModel::onPlayerToggled,
            onNumberChanged = editionViewModel::onNumberChanged,
            onClearAll = editionViewModel::onClearAll,
            onCancelClick = editionViewModel::onCancel,
            onSaveClick = editionViewModel::onSave
        )

        if (form.showTournamentSheet) {
            TournamentPickerSheet(
                tournaments = editionUiState.tournaments,
                selectedTournamentId = form.tournament?.id,
                onDismiss = editionViewModel::onTournamentSheetDismiss,
                onTournamentSelected = editionViewModel::onTournamentSelected
            )
        }

        if (form.showDiscardSheet) {
            ConfirmSheet(
                title = stringResource(R.string.discard_change_title),
                message = stringResource(R.string.discard_change_message),
                confirmLabel = stringResource(R.string.action_discard),
                cancelLabel = stringResource(R.string.action_keep_editing),
                onDismiss = editionViewModel::onDiscardDismiss,
                onConfirm = editionViewModel::onDiscardConfirm
            )
        }
    }
}

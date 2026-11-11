/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.LoadFailedState
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer
import com.telen.easylineup.lineup.create.TournamentPickerSheet
import com.telen.easylineup.lineup.edition.EditLineupSheet
import com.telen.easylineup.lineup.edition.EditLineupStep
import com.telen.easylineup.lineup.edition.InvalidLineupName
import com.telen.easylineup.lineup.edition.InvalidTournament
import com.telen.easylineup.lineup.edition.LineupEditionViewModel
import com.telen.easylineup.lineup.edition.LineupUpdateFailed
import com.telen.easylineup.lineup.edition.LineupUpdated
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

/** Replaces `TournamentListFragment` - the `navigation_lineups` root tab. */
@Composable
fun TournamentListRoute(navController: NavController) {
    val viewModel = koinViewModel<LineupListViewModel>()
    val teamViewModel = koinViewModel<TeamViewModel>()
    val editionViewModel = koinViewModel<LineupEditionViewModel>()
    val toggleRosterPlayer = koinInject<ToggleRosterPlayer>()
    val clearRoster = koinInject<ClearRoster>()
    val overrideRosterPlayerNumber = koinInject<OverrideRosterPlayerNumber>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val teamUiState by teamViewModel.uiState.collectAsStateWithLifecycle()
    val editionUiState by editionViewModel.uiState.collectAsStateWithLifecycle()

    var tournamentPendingDeletion by rememberSaveable { mutableStateOf<Tournament?>(null) }

    // The Edit Lineup sheet's own in-progress scratch fields - local to this Route since
    // LineupEditionViewModel has no screen of its own, only load()/save() actions (see its
    // own doc comment).
    var editLineupLineup by remember { mutableStateOf<Lineup?>(null) }
    var editLineupName by remember { mutableStateOf("") }
    var editLineupNameError by remember { mutableStateOf<String?>(null) }
    var editLineupSelectedTournament by remember { mutableStateOf<Tournament?>(null) }
    var editLineupTournamentError by remember { mutableStateOf<String?>(null) }
    var editLineupRoster by remember {
        mutableStateOf(TeamRosterSummary(Constants.STATUS_ALL, emptyList()))
    }
    var editLineupStep by remember { mutableStateOf(EditLineupStep.FIELDS) }
    var showEditLineupSheet by remember { mutableStateOf(false) }
    var showEditLineupDiscardSheet by remember { mutableStateOf(false) }
    var showTournamentSheetForEdit by remember { mutableStateOf(false) }
    var editLineupOriginalSnapshot by remember {
        mutableStateOf<Triple<String, Long?, TeamRosterSummary>?>(null)
    }

    fun closeEditLineupSheet() {
        showEditLineupSheet = false
        showEditLineupDiscardSheet = false
        editLineupStep = EditLineupStep.FIELDS
    }

    fun handleEditLineupCancel() {
        val snapshot = editLineupOriginalSnapshot
        val current = Triple(editLineupName, editLineupSelectedTournament?.id, editLineupRoster)
        if (snapshot == null || snapshot != current) {
            showEditLineupDiscardSheet = true
        } else {
            closeEditLineupSheet()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                Toast.makeText(
                    context,
                    context.getString(R.string.generic_error_with_detail, event.detail),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    LaunchedEffect(editionViewModel) {
        editionViewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    LineupUpdated -> closeEditLineupSheet()

                    InvalidLineupName ->
                        editLineupNameError =
                            context.getString(R.string.lineup_creation_error_name_empty)

                    InvalidTournament ->
                        editLineupTournamentError = context.getString(
                            R.string.lineup_creation_error_tournament_empty
                        )

                    LineupUpdateFailed -> Toast.makeText(
                        context,
                        R.string.error_lineup_save_failed,
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
        onEditLineupClick = { lineup ->
            coroutineScope.launch {
                editionViewModel.load(lineup.id)
                    .onSuccess { (loadedLineup, roster) ->
                        editLineupLineup = loadedLineup
                        editLineupName = loadedLineup.name
                        editLineupNameError = null
                        editLineupSelectedTournament =
                            editionUiState.tournaments.find { it.id == loadedLineup.tournamentId }
                        editLineupTournamentError = null
                        editLineupRoster = roster
                        editLineupStep = EditLineupStep.FIELDS
                        editLineupOriginalSnapshot =
                            Triple(loadedLineup.name, loadedLineup.tournamentId, roster)
                        showEditLineupSheet = true
                    }
                    .onFailure { Timber.e(it) }
            }
        },
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
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlString)))
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

    if (showEditLineupSheet) {
        EditLineupSheet(
            name = editLineupName,
            nameError = editLineupNameError,
            selectedTournament = editLineupSelectedTournament,
            tournamentError = editLineupTournamentError,
            roster = editLineupRoster,
            step = editLineupStep,
            teamType = uiState.teamType,
            onDismiss = { handleEditLineupCancel() },
            onNameChanged = {
                editLineupName = it
                editLineupNameError = null
            },
            onTournamentClick = { showTournamentSheetForEdit = true },
            onRosterClick = { editLineupStep = EditLineupStep.ROSTER },
            onRosterBackClick = { editLineupStep = EditLineupStep.FIELDS },
            onPlayerToggled = { position, checked ->
                editLineupRoster = toggleRosterPlayer(editLineupRoster, position, checked)
            },
            onNumberChanged = { position, number ->
                editLineupRoster = overrideRosterPlayerNumber(
                    editLineupRoster,
                    position,
                    number,
                    editLineupLineup?.id ?: 0L
                )
            },
            onClearAll = { editLineupRoster = clearRoster(editLineupRoster) },
            onCancelClick = { handleEditLineupCancel() },
            onSaveClick = {
                val base = editLineupLineup
                if (base != null) {
                    val updated = base.copy(
                        name = editLineupName,
                        tournamentId = editLineupSelectedTournament?.id ?: 0
                    )
                    editionViewModel.save(updated, editLineupRoster)
                }
            }
        )
    }

    if (showTournamentSheetForEdit) {
        TournamentPickerSheet(
            tournaments = editionUiState.tournaments,
            selectedTournamentId = editLineupSelectedTournament?.id,
            onDismiss = { showTournamentSheetForEdit = false },
            onTournamentSelected = { tournament ->
                editLineupSelectedTournament = tournament
                editLineupTournamentError = null
            }
        )
    }

    if (showEditLineupDiscardSheet) {
        ConfirmSheet(
            title = stringResource(R.string.discard_change_title),
            message = stringResource(R.string.discard_change_message),
            confirmLabel = stringResource(R.string.action_discard),
            cancelLabel = stringResource(R.string.action_keep_editing),
            onDismiss = { showEditLineupDiscardSheet = false },
            onConfirm = { closeEditLineupSheet() }
        )
    }
}

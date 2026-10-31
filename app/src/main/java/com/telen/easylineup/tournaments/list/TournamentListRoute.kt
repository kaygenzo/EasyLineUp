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
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.create.TournamentPickerSheet
import com.telen.easylineup.lineup.edition.EditLineupSheet
import com.telen.easylineup.lineup.edition.EditLineupStep
import com.telen.easylineup.lineup.edition.InvalidLineupName
import com.telen.easylineup.lineup.edition.InvalidTournament
import com.telen.easylineup.lineup.edition.LineupEditionViewModel
import com.telen.easylineup.lineup.edition.LineupUpdated
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

private fun TeamRosterSummary.withToggledPlayer(position: Int, status: Boolean): TeamRosterSummary {
    val updatedPlayers = players.mapIndexed { index, player ->
        if (index == position) player.copy(status = status) else player
    }
    val allSelected = updatedPlayers.all { it.status }
    return TeamRosterSummary(
        status = if (allSelected) Constants.STATUS_ALL else Constants.STATUS_PARTIAL,
        players = updatedPlayers
    )
}

private fun TeamRosterSummary.withAllCleared(): TeamRosterSummary {
    return TeamRosterSummary(
        status = Constants.STATUS_PARTIAL,
        players = players.map { it.copy(status = false) }
    )
}

private fun TeamRosterSummary.withNumberOverride(
    position: Int,
    number: Int,
    lineupId: Long
): TeamRosterSummary {
    val updatedPlayers = players.mapIndexed { index, rosterPlayer ->
        if (index == position) {
            val overlay = rosterPlayer.playerNumberOverlay?.copy(number = number)
                ?: PlayerNumberOverlay(
                    lineupId = lineupId,
                    playerId = rosterPlayer.player.id,
                    number = number
                )
            rosterPlayer.copy(playerNumberOverlay = overlay)
        } else {
            rosterPlayer
        }
    }
    return copy(players = updatedPlayers)
}

/** Replaces `TournamentListFragment` - the `navigation_lineups` root tab. */
@Composable
fun TournamentListRoute(navController: NavController) {
    val viewModel = koinViewModel<LineupViewModel>()
    val teamViewModel = koinViewModel<TeamViewModel>()
    val editionViewModel = koinViewModel<LineupEditionViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var teamName by remember { mutableStateOf("") }
    var teamType by remember { mutableStateOf(TeamType.UNKNOWN) }
    var tournamentItems by remember { mutableStateOf<List<TournamentItem>>(emptyList()) }
    var mapsByTournamentId by remember { mutableStateOf<Map<Long, GeoLocation>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var tournamentPendingDeletion by remember { mutableStateOf<Tournament?>(null) }

    var editionTournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
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

    LaunchedEffect(viewModel, teamViewModel, editionViewModel) {
        teamViewModel.observeCurrentTeamName()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(this)

        viewModel.mapsFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { (tournament, location) ->
                mapsByTournamentId = mapsByTournamentId + (tournament.id to location)
            }
            .launchIn(this)

        viewModel.observeCategorizedLineups()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { tournamentItems = it }
            .launchIn(this)

        editionViewModel.getTournaments()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { editionTournaments = it }
            .launchIn(this)

        launch {
            viewModel.getTeamType()
                .onSuccess { teamType = TeamType.getTypeById(it) }
                .onFailure { Timber.e(it) }
        }

        launch {
            editionViewModel.events
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        LineupUpdated -> {
                            closeEditLineupSheet()
                            viewModel.reloadList()
                        }

                        InvalidLineupName ->
                            editLineupNameError =
                                context.getString(R.string.lineup_creation_error_name_empty)

                        InvalidTournament ->
                            editLineupTournamentError = context.getString(
                                R.string.lineup_creation_error_tournament_empty
                            )
                    }
                }
        }

        viewModel.setFilter("")
    }

    TournamentListScreen(
        state = TournamentListState(
            teamName = teamName,
            teamType = teamType,
            items = tournamentItems,
            mapsByTournamentId = mapsByTournamentId,
            searchQuery = searchQuery
        ),
        onBackClick = { navController.navigateUp() },
        onSearchQueryChanged = {
            searchQuery = it
            viewModel.setFilter(it)
        },
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
                            editionTournaments.find { it.id == loadedLineup.tournamentId }
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
                    teamType = teamType.id
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
                coroutineScope.launch {
                    viewModel.deleteTournament(tournament)
                        .onFailure { throwable ->
                            Timber.e(throwable)
                            Toast.makeText(
                                context,
                                "Something wrong happened: ${throwable.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
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
            teamType = teamType,
            onDismiss = { handleEditLineupCancel() },
            onNameChanged = {
                editLineupName = it
                editLineupNameError = null
            },
            onTournamentClick = { showTournamentSheetForEdit = true },
            onRosterClick = { editLineupStep = EditLineupStep.ROSTER },
            onRosterBackClick = { editLineupStep = EditLineupStep.FIELDS },
            onPlayerToggled = { position, checked ->
                editLineupRoster = editLineupRoster.withToggledPlayer(position, checked)
            },
            onNumberChanged = { position, number ->
                editLineupRoster = editLineupRoster.withNumberOverride(
                    position,
                    number,
                    editLineupLineup?.id ?: 0L
                )
            },
            onClearAll = { editLineupRoster = editLineupRoster.withAllCleared() },
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
            tournaments = editionTournaments,
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

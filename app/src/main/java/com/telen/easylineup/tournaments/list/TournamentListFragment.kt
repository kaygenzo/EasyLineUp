/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.lineup.create.TournamentPickerSheet
import com.telen.easylineup.lineup.edition.EditLineupSheet
import com.telen.easylineup.lineup.edition.EditLineupStep
import com.telen.easylineup.lineup.edition.InvalidLineupName
import com.telen.easylineup.lineup.edition.InvalidTournament
import com.telen.easylineup.lineup.edition.LineupEditionViewModel
import com.telen.easylineup.lineup.edition.LineupUpdated
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
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

class TournamentListFragment : BaseFragment("TournamentListFragment") {
    private val viewModel by viewModels<LineupViewModel>()
    private val teamViewModel: TeamViewModel by viewModel()
    private val editionViewModel: LineupEditionViewModel by viewModel()

    private var teamName by mutableStateOf("")
    private var teamType by mutableStateOf(TeamType.UNKNOWN)
    private var tournamentItems by mutableStateOf<List<TournamentItem>>(emptyList())
    private var mapsByTournamentId by mutableStateOf<Map<Long, GeoLocation>>(emptyMap())
    private var searchQuery by mutableStateOf("")
    private var tournamentPendingDeletion by mutableStateOf<Tournament?>(null)

    private var editionTournaments by mutableStateOf<List<Tournament>>(emptyList())
    private var editLineupLineup by mutableStateOf<Lineup?>(null)
    private var editLineupName by mutableStateOf("")
    private var editLineupNameError by mutableStateOf<String?>(null)
    private var editLineupSelectedTournament by mutableStateOf<Tournament?>(null)
    private var editLineupTournamentError by mutableStateOf<String?>(null)
    private var editLineupRoster by mutableStateOf(
        TeamRosterSummary(Constants.STATUS_ALL, emptyList())
    )
    private var editLineupStep by mutableStateOf(EditLineupStep.FIELDS)
    private var showEditLineupSheet by mutableStateOf(false)
    private var showEditLineupDiscardSheet by mutableStateOf(false)
    private var showTournamentSheetForEdit by mutableStateOf(false)
    private var editLineupOriginalSnapshot: Triple<String, Long?, TeamRosterSummary>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    TournamentListScreen(
                        state = TournamentListState(
                            teamName = teamName,
                            teamType = teamType,
                            items = tournamentItems,
                            mapsByTournamentId = mapsByTournamentId,
                            searchQuery = searchQuery
                        ),
                        onBackClick = { findNavController().navigateUp() },
                        onSearchQueryChanged = {
                            searchQuery = it
                            viewModel.setFilter(it)
                        },
                        onCreateLineupClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_create")
                            findNavController().navigate(
                                R.id.lineupCreationFragment,
                                null,
                                NavigationUtils().getOptions()
                            )
                        },
                        onLineupClick = ::onLineupClicked,
                        onEditLineupClick = ::onEditLineupClicked,
                        onStatsClick = ::onStatisticsTournamentClicked,
                        onDeleteClick = { tournamentPendingDeletion = it },
                        onMapClick = ::onMapClicked
                    )

                    tournamentPendingDeletion?.let { tournament ->
                        ConfirmSheet(
                            title = stringResource(
                                R.string.dialog_delete_tournament_title,
                                tournament.name
                            ),
                            message = stringResource(R.string.dialog_delete_cannot_undo_message),
                            confirmLabel = stringResource(R.string.action_delete),
                            cancelLabel = stringResource(R.string.cancel_button),
                            onDismiss = { tournamentPendingDeletion = null },
                            onConfirm = {
                                FirebaseAnalyticsUtils.onClick(
                                    activity,
                                    "click_tournaments_delete"
                                )
                                deleteTournament(tournament)
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
                                editLineupRoster = editLineupRoster.withToggledPlayer(
                                    position,
                                    checked
                                )
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
                            onSaveClick = { saveEditLineup() }
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
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamViewModel.observeCurrentTeamName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.mapsFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { (tournament, mapInfo) ->
                mapInfo.location?.let { location ->
                    mapsByTournamentId = mapsByTournamentId + (tournament.id to location)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeCategorizedLineups()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { tournamentItems = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        editionViewModel.getTournaments()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { editionTournaments = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTeamType()
                .onSuccess { teamType = TeamType.getTypeById(it) }
                .onFailure { Timber.e(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                editionViewModel.events.collect { event ->
                    when (event) {
                        LineupUpdated -> {
                            closeEditLineupSheet()
                            viewModel.reloadList()
                        }

                        InvalidLineupName ->
                            editLineupNameError =
                                getString(R.string.lineup_creation_error_name_empty)

                        InvalidTournament ->
                            editLineupTournamentError =
                                getString(R.string.lineup_creation_error_tournament_empty)
                    }
                }
            }
        }

        viewModel.setFilter("")
    }

    private fun onStatisticsTournamentClicked(tournament: Tournament) {
        FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_stats")
        val extras = Bundle()
        extras.putSerializable(Constants.EXTRA_TOURNAMENT, tournament)
        extras.putInt(Constants.EXTRA_TEAM_TYPE, teamType.id)
        findNavController().navigate(
            R.id.tournamentStatisticsTableFragment,
            extras,
            NavigationUtils().getOptions()
        )
    }

    private fun onEditLineupClicked(lineup: Lineup) {
        viewLifecycleOwner.lifecycleScope.launch {
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
    }

    private fun handleEditLineupCancel() {
        val snapshot = editLineupOriginalSnapshot
        val current = Triple(
            editLineupName,
            editLineupSelectedTournament?.id,
            editLineupRoster
        )
        if (snapshot == null || snapshot != current) {
            showEditLineupDiscardSheet = true
        } else {
            closeEditLineupSheet()
        }
    }

    private fun closeEditLineupSheet() {
        showEditLineupSheet = false
        showEditLineupDiscardSheet = false
        editLineupStep = EditLineupStep.FIELDS
    }

    private fun saveEditLineup() {
        val base = editLineupLineup ?: return
        val updated = base.copy(
            name = editLineupName,
            tournamentId = editLineupSelectedTournament?.id ?: 0
        )
        editionViewModel.save(updated, editLineupRoster)
    }

    private fun onMapClicked(location: GeoLocation) {
        val urlString = "geo:${location.latitude},${location.longitude}"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlString)))
    }

    private fun onLineupClicked(lineup: Lineup) {
        FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_lineup_selected")
        findNavController().navigate(
            R.id.lineupFragmentFixed,
            LineupFragment.getArguments(lineup.id),
            NavigationUtils().getOptions()
        )
    }

    private fun deleteTournament(tournament: Tournament) {
        val activity = activity ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteTournament(tournament)
                .onFailure { throwable ->
                    Timber.e(throwable)
                    Toast.makeText(
                        activity,
                        "Something wrong happened: ${throwable.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}

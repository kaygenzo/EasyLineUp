/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.LineupFragment
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

class LineupCreationFragment : BaseFragment("LineupCreationFragment") {
    private val viewModel: LineupCreationViewModel by viewModel()

    private var teamType by mutableStateOf(TeamType.UNKNOWN)
    private var name by mutableStateOf("")
    private var nameError by mutableStateOf<String?>(null)
    private var tournaments by mutableStateOf<List<Tournament>>(emptyList())
    private var selectedTournament by mutableStateOf<Tournament?>(null)
    private var tournamentError by mutableStateOf<String?>(null)
    private var eventTimeInMillis by mutableStateOf(System.currentTimeMillis())
    private var roster by mutableStateOf(TeamRosterSummary(Constants.STATUS_ALL, emptyList()))
    private var strategy by mutableStateOf(TeamStrategy.STANDARD)
    private var extraHitters by mutableStateOf(0)
    private var showTournamentSheet by mutableStateOf(false)
    private var showRosterSheet by mutableStateOf(false)
    private var showDiscardSheet by mutableStateOf(false)
    private var showNewTournamentSheet by mutableStateOf(false)
    private var newTournamentName by mutableStateOf("")
    private var newTournamentStartTime by mutableStateOf(System.currentTimeMillis())
    private var newTournamentEndTime by mutableStateOf(System.currentTimeMillis())
    private var newTournamentAddress by mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    LineupCreationScreen(
                        state = LineupCreationState(
                            teamType = teamType,
                            name = name,
                            nameError = nameError,
                            tournaments = tournaments,
                            selectedTournament = selectedTournament,
                            tournamentError = tournamentError,
                            eventTimeInMillis = eventTimeInMillis,
                            roster = roster,
                            strategy = strategy,
                            extraHitters = extraHitters
                        ),
                        onBackClick = { handleCancel() },
                        onNameChanged = {
                            name = it
                            nameError = null
                        },
                        onTournamentClick = { showTournamentSheet = true },
                        onDateClick = { selectedTournament?.let(::showDatePicker) },
                        onRosterClick = { showRosterSheet = true },
                        onStrategySelected = { strategy = it },
                        onExtraHittersSelected = { extraHitters = it },
                        onCancelClick = { handleCancel() },
                        onSaveClick = { saveLineup() }
                    )

                    if (showTournamentSheet) {
                        TournamentPickerSheet(
                            tournaments = tournaments,
                            selectedTournamentId = selectedTournament?.id,
                            onDismiss = { showTournamentSheet = false },
                            onTournamentSelected = { tournament ->
                                selectedTournament = tournament
                                tournamentError = null
                                updateEventTimeForTournament(tournament)
                            },
                            onCreateTournamentClick = {
                                showTournamentSheet = false
                                newTournamentName = ""
                                newTournamentStartTime = System.currentTimeMillis()
                                newTournamentEndTime = System.currentTimeMillis()
                                newTournamentAddress = ""
                                showNewTournamentSheet = true
                            }
                        )
                    }

                    if (showRosterSheet) {
                        RosterPickerSheet(
                            roster = roster,
                            teamType = teamType,
                            onDismiss = { showRosterSheet = false },
                            onPlayerToggled = { position, checked ->
                                roster = roster.withToggledPlayer(position, checked)
                            },
                            onClearAll = { roster = roster.withAllCleared() }
                        )
                    }

                    if (showNewTournamentSheet) {
                        NewTournamentSheet(
                            name = newTournamentName,
                            startTimeInMillis = newTournamentStartTime,
                            endTimeInMillis = newTournamentEndTime,
                            address = newTournamentAddress,
                            onNameChanged = { newTournamentName = it },
                            onStartDateClick = { showNewTournamentDatePicker(isStart = true) },
                            onEndDateClick = { showNewTournamentDatePicker(isStart = false) },
                            onAddressChanged = { newTournamentAddress = it },
                            onDismiss = { showNewTournamentSheet = false },
                            onAddClick = { addNewTournament() }
                        )
                    }

                    if (showDiscardSheet) {
                        ConfirmSheet(
                            title = stringResource(R.string.discard_change_title),
                            message = stringResource(R.string.discard_change_message),
                            confirmLabel = stringResource(R.string.action_discard),
                            cancelLabel = stringResource(R.string.action_keep_editing),
                            onDismiss = { showDiscardSheet = false },
                            onConfirm = { findNavController().popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleCancel()
        }

        viewModel.getTournaments()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { tournaments = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTeamType()
                .onSuccess { teamType = TeamType.getTypeById(it) }
                .onFailure { Timber.e(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadRoster()
                .onSuccess { roster = it }
                .onFailure { Timber.e(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is LineupSaved -> {
                            findNavController().navigate(
                                R.id.lineupFragmentEditable,
                                LineupFragment.getArguments(event.lineupId),
                                NavigationUtils().getOptionsWithPopDestination(
                                    R.id.navigation_lineups,
                                    false
                                )
                            )
                        }

                        InvalidLineupName ->
                            nameError = getString(R.string.lineup_creation_error_name_empty)

                        InvalidTournament ->
                            tournamentError =
                                getString(R.string.lineup_creation_error_tournament_empty)
                    }
                }
            }
        }
    }

    private fun updateEventTimeForTournament(tournament: Tournament) {
        eventTimeInMillis = if (tournament.startTime > 0 && tournament.endTime > 0) {
            val outsideRange = tournament.startTime > eventTimeInMillis ||
                eventTimeInMillis > tournament.endTime
            if (outsideRange) {
                tournament.startTime
            } else {
                eventTimeInMillis
            }
        } else {
            System.currentTimeMillis()
        }
    }

    private fun showDatePicker(tournament: Tournament) {
        var constraints: CalendarConstraints? = null
        if (tournament.startTime > 0 && tournament.endTime > 0) {
            val min = DateValidatorPointForward.from(tournament.startTime)
            val max = DateValidatorPointBackward.before(tournament.endTime)
            constraints = CalendarConstraints.Builder()
                .setValidator(CompositeDateValidator.allOf(listOf(min, max)))
                .build()
        }
        val builder = MaterialDatePicker.Builder.datePicker().setSelection(eventTimeInMillis)
        constraints?.let { builder.setCalendarConstraints(it) }
        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener { selection -> eventTimeInMillis = selection }
        datePicker.show(childFragmentManager, "createLineupDatePicker")
    }

    private fun showNewTournamentDatePicker(isStart: Boolean) {
        val initialTime = if (isStart) newTournamentStartTime else newTournamentEndTime
        val datePicker = MaterialDatePicker.Builder.datePicker().setSelection(initialTime).build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            if (isStart) {
                newTournamentStartTime = selection
            } else {
                newTournamentEndTime = selection
            }
        }
        val tag = if (isStart) "newTournamentStartDatePicker" else "newTournamentEndDatePicker"
        datePicker.show(childFragmentManager, tag)
    }

    private fun addNewTournament() {
        val tournament = Tournament(
            name = newTournamentName,
            createdAt = System.currentTimeMillis(),
            startTime = newTournamentStartTime,
            endTime = newTournamentEndTime,
            address = newTournamentAddress.takeIf { it.isNotBlank() }
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveTournamentInline(tournament)
                .onSuccess {
                    selectedTournament = tournament
                    tournamentError = null
                    updateEventTimeForTournament(tournament)
                    showNewTournamentSheet = false
                }
                .onFailure { Timber.e(it) }
        }
    }

    private fun handleCancel() {
        if (name.isNotBlank() || selectedTournament != null) {
            showDiscardSheet = true
        } else {
            findNavController().popBackStack()
        }
    }

    private fun saveLineup() {
        val lineup = Lineup(
            name = name,
            tournamentId = selectedTournament?.id ?: 0,
            strategy = strategy.id,
            extraHitters = extraHitters,
            eventTimeInMillis = eventTimeInMillis
        )
        viewModel.save(lineup, roster)
    }
}

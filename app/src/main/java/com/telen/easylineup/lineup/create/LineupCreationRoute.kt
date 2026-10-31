/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.navigation.Routes
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

/** Replaces `LineupCreationFragment`. */
@Composable
fun LineupCreationRoute(navController: NavController) {
    val viewModel = koinViewModel<LineupCreationViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var teamType by remember { mutableStateOf(TeamType.UNKNOWN) }
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var tournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var selectedTournament by remember { mutableStateOf<Tournament?>(null) }
    var tournamentError by remember { mutableStateOf<String?>(null) }
    var eventTimeInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var roster by remember {
        mutableStateOf(TeamRosterSummary(Constants.STATUS_ALL, emptyList()))
    }
    var strategy by remember { mutableStateOf(TeamStrategy.STANDARD) }
    var extraHitters by remember { mutableStateOf(0) }
    var showTournamentSheet by remember { mutableStateOf(false) }
    var showRosterSheet by remember { mutableStateOf(false) }
    var showDiscardSheet by remember { mutableStateOf(false) }
    var showNewTournamentSheet by remember { mutableStateOf(false) }
    var newTournamentName by remember { mutableStateOf("") }
    var newTournamentStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var newTournamentEndTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var newTournamentAddress by remember { mutableStateOf("") }

    fun updateEventTimeForTournament(tournament: Tournament) {
        eventTimeInMillis = if (tournament.startTime > 0 && tournament.endTime > 0) {
            val outsideRange = tournament.startTime > eventTimeInMillis ||
                eventTimeInMillis > tournament.endTime
            if (outsideRange) tournament.startTime else eventTimeInMillis
        } else {
            System.currentTimeMillis()
        }
    }

    fun handleCancel() {
        if (name.isNotBlank() || selectedTournament != null) {
            showDiscardSheet = true
        } else {
            navController.popBackStack()
        }
    }

    fun showDatePicker(tournament: Tournament) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
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
        datePicker.show(fragmentManager, "createLineupDatePicker")
    }

    fun showNewTournamentDatePicker(isStart: Boolean) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        val initialTime = if (isStart) newTournamentStartTime else newTournamentEndTime
        val datePicker = MaterialDatePicker.Builder.datePicker().setSelection(initialTime).build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            if (isStart) newTournamentStartTime = selection else newTournamentEndTime = selection
        }
        val tag = if (isStart) "newTournamentStartDatePicker" else "newTournamentEndDatePicker"
        datePicker.show(fragmentManager, tag)
    }

    BackHandler { handleCancel() }

    LaunchedEffect(viewModel) {
        viewModel.getTournaments()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { tournaments = it }
            .launchIn(this)

        launch {
            viewModel.getTeamType()
                .onSuccess {
                    val resolvedType = TeamType.getTypeById(it)
                    teamType = resolvedType
                    // The strategy picker is only shown for team types with more than one
                    // valid strategy (getStrategiesDisplayName() returns null otherwise, e.g.
                    // BASEBALL_5) - for those, this default is never offered to the user to
                    // confirm or change, so it must already be correct for the team's type.
                    strategy = resolvedType.defaultStrategy
                }
                .onFailure { Timber.e(it) }
        }

        launch {
            viewModel.loadRoster()
                .onSuccess { roster = it }
                .onFailure { Timber.e(it) }
        }

        launch {
            viewModel.events
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        is LineupSaved -> {
                            navController.navigate(
                                Routes.Lineup(lineupId = event.lineupId, editable = true)
                            ) {
                                popUpTo<Routes.Tournaments> { inclusive = false }
                            }
                        }

                        InvalidLineupName ->
                            nameError = context.getString(R.string.lineup_creation_error_name_empty)

                        InvalidTournament ->
                            tournamentError = context.getString(
                                R.string.lineup_creation_error_tournament_empty
                            )
                    }
                }
        }
    }

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
        onSaveClick = {
            val lineup = Lineup(
                name = name,
                tournamentId = selectedTournament?.id ?: 0,
                strategy = strategy.id,
                extraHitters = extraHitters,
                eventTimeInMillis = eventTimeInMillis
            )
            viewModel.save(lineup, roster)
        }
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
            onAddClick = {
                val tournament = Tournament(
                    name = newTournamentName,
                    createdAt = System.currentTimeMillis(),
                    startTime = newTournamentStartTime,
                    endTime = newTournamentEndTime,
                    address = newTournamentAddress.takeIf { it.isNotBlank() }
                )
                coroutineScope.launch {
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
        )
    }

    if (showDiscardSheet) {
        ConfirmSheet(
            title = stringResource(R.string.discard_change_title),
            message = stringResource(R.string.discard_change_message),
            confirmLabel = stringResource(R.string.action_discard),
            cancelLabel = stringResource(R.string.action_keep_editing),
            onDismiss = { showDiscardSheet = false },
            onConfirm = { navController.popBackStack() }
        )
    }
}

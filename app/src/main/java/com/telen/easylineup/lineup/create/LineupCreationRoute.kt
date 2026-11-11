/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.hasDateWindow
import com.telen.easylineup.navigation.Routes
import org.koin.androidx.compose.koinViewModel

/**
 * Replaces `LineupCreationFragment`. [LineupCreationViewModel.uiState] drives the whole screen
 * and its sheets; the Route only hosts the platform pieces a ViewModel can't own (navigation,
 * the native [MaterialDatePicker]).
 */
@Composable
fun LineupCreationRoute(navController: NavController) {
    val viewModel = koinViewModel<LineupCreationViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun handleCancel() {
        if (viewModel.onCancelRequested()) {
            navController.popBackStack()
        }
    }

    fun showDatePicker(tournament: Tournament) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        var constraints: CalendarConstraints? = null
        if (tournament.hasDateWindow()) {
            val min = DateValidatorPointForward.from(tournament.startTime)
            val max = DateValidatorPointBackward.before(tournament.endTime)
            constraints = CalendarConstraints.Builder()
                .setValidator(CompositeDateValidator.allOf(listOf(min, max)))
                .build()
        }
        val builder = MaterialDatePicker.Builder.datePicker()
            .setSelection(uiState.eventTimeInMillis)
        constraints?.let { builder.setCalendarConstraints(it) }
        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            viewModel.onEventTimeSelected(selection)
        }
        datePicker.show(fragmentManager, "createLineupDatePicker")
    }

    fun showNewTournamentDatePicker(isStart: Boolean) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        val initialTime = if (isStart) {
            uiState.newTournamentStartTime
        } else {
            uiState.newTournamentEndTime
        }
        val datePicker = MaterialDatePicker.Builder.datePicker().setSelection(initialTime).build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            if (isStart) {
                viewModel.onNewTournamentStartTimeSelected(selection)
            } else {
                viewModel.onNewTournamentEndTimeSelected(selection)
            }
        }
        val tag = if (isStart) "newTournamentStartDatePicker" else "newTournamentEndDatePicker"
        datePicker.show(fragmentManager, tag)
    }

    BackHandler { handleCancel() }

    LaunchedEffect(viewModel) {
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
                }
            }
    }

    LineupCreationScreen(
        state = uiState,
        onBackClick = { handleCancel() },
        onNameChanged = viewModel::onNameChanged,
        onTournamentClick = viewModel::onTournamentSheetOpen,
        onDateClick = { uiState.selectedTournament?.let(::showDatePicker) },
        onRosterClick = viewModel::onRosterSheetOpen,
        onStrategySelected = viewModel::onStrategySelected,
        onExtraHittersSelected = viewModel::onExtraHittersSelected,
        onCancelClick = { handleCancel() },
        onSaveClick = viewModel::onSaveClick
    )

    if (uiState.showTournamentSheet) {
        TournamentPickerSheet(
            tournaments = uiState.tournaments,
            selectedTournamentId = uiState.selectedTournament?.id,
            onDismiss = viewModel::onTournamentSheetDismiss,
            onTournamentSelected = viewModel::onTournamentSelected,
            onCreateTournamentClick = viewModel::onNewTournamentSheetOpen
        )
    }

    if (uiState.showRosterSheet) {
        RosterPickerSheet(
            roster = uiState.roster,
            teamType = uiState.teamType,
            onDismiss = viewModel::onRosterSheetDismiss,
            onPlayerToggled = viewModel::onRosterPlayerToggled,
            onClearAll = viewModel::onRosterClearAll
        )
    }

    if (uiState.showNewTournamentSheet) {
        NewTournamentSheet(
            name = uiState.newTournamentName,
            startTimeInMillis = uiState.newTournamentStartTime,
            endTimeInMillis = uiState.newTournamentEndTime,
            address = uiState.newTournamentAddress,
            onNameChanged = viewModel::onNewTournamentNameChanged,
            onStartDateClick = { showNewTournamentDatePicker(isStart = true) },
            onEndDateClick = { showNewTournamentDatePicker(isStart = false) },
            onAddressChanged = viewModel::onNewTournamentAddressChanged,
            onDismiss = viewModel::onNewTournamentSheetDismiss,
            onAddClick = viewModel::onAddNewTournament
        )
    }

    if (uiState.showDiscardSheet) {
        ConfirmSheet(
            title = stringResource(R.string.discard_change_title),
            message = stringResource(R.string.discard_change_message),
            confirmLabel = stringResource(R.string.action_discard),
            cancelLabel = stringResource(R.string.action_keep_editing),
            onDismiss = viewModel::onDiscardDismiss,
            onConfirm = { navController.popBackStack() }
        )
    }
}

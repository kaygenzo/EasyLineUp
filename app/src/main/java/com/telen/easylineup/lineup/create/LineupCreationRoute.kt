/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutDatePickerDialog
import com.telen.easylineup.domain.model.hasDateWindow
import com.telen.easylineup.navigation.Routes
import org.koin.androidx.compose.koinViewModel

/**
 * Replaces `LineupCreationFragment`. [LineupCreationViewModel.uiState] drives the whole screen
 * and its sheets - date pickers included; the Route only hosts the platform pieces a ViewModel
 * can't own (navigation).
 */
@Composable
fun LineupCreationRoute(navController: NavController) {
    val viewModel = koinViewModel<LineupCreationViewModel>()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun handleCancel() {
        if (viewModel.onCancelRequested()) {
            navController.popBackStack()
        }
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
                    LineupSaveFailed -> Toast.makeText(
                        context,
                        R.string.error_lineup_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    NewTournamentSaveFailed -> Toast.makeText(
                        context,
                        R.string.error_tournament_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    LineupCreationScreen(
        state = uiState,
        onBackClick = { handleCancel() },
        onNameChanged = viewModel::onNameChanged,
        onTournamentClick = viewModel::onTournamentSheetOpen,
        onDateClick = viewModel::onEventDateClick,
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
            nameErrorText = when (uiState.newTournamentError) {
                NewTournamentError.NameEmpty ->
                    stringResource(R.string.lineup_creation_error_tournament_empty)
                NewTournamentError.NameTaken ->
                    stringResource(R.string.lineup_creation_error_tournament_name_taken)
                null -> null
            },
            startTimeInMillis = uiState.newTournamentStartTime,
            endTimeInMillis = uiState.newTournamentEndTime,
            address = uiState.newTournamentAddress,
            onNameChanged = viewModel::onNewTournamentNameChanged,
            onStartDateClick = viewModel::onNewTournamentStartDateClick,
            onEndDateClick = viewModel::onNewTournamentEndDateClick,
            onAddressChanged = viewModel::onNewTournamentAddressChanged,
            onDismiss = viewModel::onNewTournamentSheetDismiss,
            onAddClick = viewModel::onAddNewTournament
        )
    }

    uiState.openDatePicker?.let { picker ->
        val tournament = uiState.selectedTournament
        when (picker) {
            LineupDatePicker.EVENT -> DugoutDatePickerDialog(
                initialDateMillis = uiState.eventTimeInMillis,
                onDateSelected = viewModel::onDatePicked,
                onDismiss = viewModel::onDatePickerDismiss,
                firstSelectableDateMillis = tournament?.takeIf { it.hasDateWindow() }?.startTime,
                lastSelectableDateMillis = tournament?.takeIf { it.hasDateWindow() }?.endTime
            )
            LineupDatePicker.NEW_TOURNAMENT_START -> DugoutDatePickerDialog(
                initialDateMillis = uiState.newTournamentStartTime,
                onDateSelected = viewModel::onDatePicked,
                onDismiss = viewModel::onDatePickerDismiss
            )
            LineupDatePicker.NEW_TOURNAMENT_END -> DugoutDatePickerDialog(
                initialDateMillis = uiState.newTournamentEndTime,
                onDateSelected = viewModel::onDatePicked,
                onDismiss = viewModel::onDatePickerDismiss
            )
        }
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

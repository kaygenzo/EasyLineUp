/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.lineup.defense.AvailablePlayersSheet
import com.telen.easylineup.lineup.defense.DpFlexLinkSheet
import com.telen.easylineup.lineup.edition.LineupEditionViewModel
import com.telen.easylineup.lineup.edition.LineupUpdated
import com.telen.easylineup.lineup.edition.RosterEditSheet
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Replaces `LineupFragment`/`LineupFragmentFixed`/`LineupFragmentEditable` - [editable]
 * collapses what used to be two separate Fragment subclasses (same base class, only
 * differing by a constructor boolean) into the one [Routes.Lineup] destination.
 * [LineupViewModel.uiState] drives the whole screen and its sheets; the Route only hosts the
 * platform pieces a ViewModel can't own (navigation, the one-shot share Intent).
 */
@Composable
fun LineupRoute(
    navController: NavController,
    lineupId: Long,
    editable: Boolean,
    fromShortcut: Boolean
) {
    val viewModel = koinViewModel<LineupViewModel>()
    val editionViewModel = koinViewModel<LineupEditionViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    remember(lineupId, editable) {
        viewModel.editable = editable
        viewModel.lineupId = lineupId
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    fun goBack(showFixedView: Boolean = true) {
        if (fromShortcut) {
            navController.popBackStack(Routes.Dashboard, false)
        } else {
            val success = !showFixedView &&
                navController.popBackStack(Routes.Tournaments, false)
            if (!success) {
                navController.popBackStack()
            }
        }
    }

    var shareJob by remember { mutableStateOf<Job?>(null) }

    fun shareLineup() {
        shareJob?.cancel()
        shareJob = coroutineScope.launch {
            runCatching { viewModel.exportLineupToExternalStorage(context) }
                .onSuccess { intent -> context.startActivity(Intent.createChooser(intent, "")) }
                .onFailure { Timber.e(it) }
        }
    }

    fun handlePositionClicked(position: FieldPosition) {
        coroutineScope.launch {
            viewModel.onPositionClicked(position)
                .onFailure { throwable ->
                    if (throwable is NeedAssignPitcherFirstException) {
                        FirebaseAnalyticsUtils.missingPitcher(context)
                    }
                }
        }
    }

    LaunchedEffect(editionViewModel) {
        editionViewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event -> if (event == LineupUpdated) viewModel.closeRosterEditSheet() }
    }

    BackHandler(enabled = editable) {
        FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit_cancel_back_pressed")
        viewModel.onDiscardSheetRequested()
    }

    LineupScreen(
        lineupName = state.lineupName,
        editable = editable,
        selectedTab = state.selectedTab,
        onTabSelected = viewModel::onTabSelected,
        onBackClick = { goBack() },
        onCancelClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit_cancel_cancel")
            viewModel.onDiscardSheetRequested()
        },
        onSaveClick = {
            coroutineScope.launch {
                viewModel.save()
                    .onSuccess {
                        Timber.d("Successfully saved")
                        // navController navigation asserts the main thread - resuming after
                        // save()'s real Room write doesn't always land back there under Compose
                        // test instrumentation (see the identical fix in PlayerEditRoute).
                        withContext(Dispatchers.Main) { goBack() }
                    }
                    .onFailure { Timber.e(it) }
            }
        },
        onShareLineup = { shareLineup() },
        onDeleteClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_delete")
            viewModel.onDeleteSheetRequested()
        },
        onEditClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit")
            navController.navigate(Routes.Lineup(lineupId = lineupId, editable = true))
        },
        onEditRosterClick = {
            coroutineScope.launch {
                editionViewModel.load(lineupId)
                    .onSuccess { (loadedLineup, roster) ->
                        viewModel.onRosterEditLoaded(loadedLineup, roster)
                    }
                    .onFailure { Timber.e(it) }
            }
        },
        defenseStrategy = state.defenseStrategy,
        defensePlayers = state.defensePlayers,
        lineupMode = state.lineupMode,
        teamType = state.teamType,
        extraHitters = state.extraHitters,
        onPositionClicked = ::handlePositionClicked,
        onPlayersSwitched = viewModel::switchPlayersPosition,
        onPlayerReassigned = viewModel::switchPlayersPosition,
        onPlayerSentToTrash = { player, _ -> viewModel.onDeletePosition(player) },
        onSubstituteLongClicked = viewModel::onSubstituteLongClicked,
        onAddSubstitute = { handlePositionClicked(FieldPosition.SUBSTITUTE) },
        onLineupModeChanged = viewModel::onLineupModeChanged,
        batters = state.batters,
        typeface = state.typeface,
        onReorder = viewModel::onBattersChanged
    )

    state.availablePlayersEvent?.let { event ->
        AvailablePlayersSheet(
            players = event.players,
            lineupPlayers = state.defensePlayers,
            filterPosition = event.position,
            teamType = state.teamType,
            extraHitters = state.extraHitters,
            onPlayerSelected = { player -> viewModel.onPlayerSelected(player, event.position) },
            onDismiss = viewModel::onAvailablePlayersDismiss
        )
    }

    state.dpFlexEvent?.let { event ->
        val dpFlexTitle = if (event.teamType == TeamType.SOFTBALL.id) {
            stringResource(R.string.link_dp_and_flex_dialog_title)
        } else {
            stringResource(R.string.link_dh_and_pitcher_dialog_title)
        }
        DpFlexLinkSheet(
            initialDp = event.initialData.first?.toPlayer(),
            initialFlex = event.initialData.second?.toPlayer(),
            dpBattingOrder = event.initialData.first?.order,
            dpLocked = event.dpLocked,
            flexLocked = event.flexLocked,
            teamType = event.teamType,
            title = dpFlexTitle,
            onRequestDpPlayers = {
                viewModel.getPlayerSelectionForDp()
                    .getOrElse { emptyList() }
                    .map { it.toPlayer() }
            },
            onRequestFlexPlayers = {
                viewModel.getPlayerSelectionForFlex()
                    .getOrElse { emptyList() }
                    .map { it.toPlayer() }
            },
            onConfirm = { dp, flex ->
                coroutineScope.launch {
                    viewModel.linkDpAndFlex(dp, flex)
                        .onFailure { throwable ->
                            if (throwable is NeedAssignBothPlayersException) {
                                FirebaseAnalyticsUtils.missingDpFlex(context)
                            }
                        }
                }
            },
            onDismiss = viewModel::onDpFlexDismiss
        )
    }

    state.deleteConfirmPlayer?.let { player ->
        ConfirmSheet(
            title = stringResource(R.string.dialog_delete_substitute_title, player.name),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = viewModel::onDeleteConfirmDismiss,
            onConfirm = { viewModel.onDeletePosition(player) }
        )
    }

    state.infoMessage?.let { message ->
        val messageRes = when (message) {
            LineupInfoMessage.NoCandidatesForPosition -> R.string.players_list_empty
            LineupInfoMessage.PitcherAssignmentRequired ->
                R.string.error_need_assign_pitcher_first_message
            LineupInfoMessage.BothDpAndFlexRequired ->
                R.string.error_need_assign_both_players_message
        }
        AlertDialog(
            onDismissRequest = viewModel::onInfoMessageDismiss,
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = viewModel::onInfoMessageDismiss) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }

    if (state.showDiscardSheet) {
        ConfirmSheet(
            title = stringResource(R.string.discard_change_title),
            message = stringResource(R.string.discard_change_message),
            confirmLabel = stringResource(R.string.action_discard),
            cancelLabel = stringResource(R.string.action_keep_editing),
            onDismiss = viewModel::onDiscardSheetDismiss,
            onConfirm = { goBack() }
        )
    }

    if (state.showDeleteSheet) {
        ConfirmSheet(
            title = stringResource(R.string.dialog_delete_lineup_title),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = viewModel::onDeleteSheetDismiss,
            onConfirm = {
                coroutineScope.launch {
                    viewModel.deleteLineup()
                        .onSuccess {
                            // See the matching comment on the save flow above.
                            withContext(Dispatchers.Main) { goBack(showFixedView = false) }
                        }
                        .onFailure {
                            Timber.e(it)
                            Toast.makeText(
                                context,
                                R.string.error_when_deleting_lineup,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        )
    }

    if (state.showRosterEditSheet) {
        RosterEditSheet(
            roster = state.rosterEditSummary,
            teamType = TeamType.getTypeById(state.teamType),
            onDismiss = viewModel::onRosterEditDismissRequested,
            onPlayerToggled = viewModel::onRosterEditPlayerToggled,
            onNumberChanged = viewModel::onRosterEditNumberChanged,
            onClearAll = viewModel::onRosterEditClearAll,
            onDoneClick = {
                state.rosterEditLineup?.let { editionViewModel.save(it, state.rosterEditSummary) }
            }
        )
    }

    if (state.showRosterEditDiscardSheet) {
        ConfirmSheet(
            title = stringResource(R.string.discard_change_title),
            message = stringResource(R.string.discard_change_message),
            confirmLabel = stringResource(R.string.action_discard),
            cancelLabel = stringResource(R.string.action_keep_editing),
            onDismiss = viewModel::onRosterEditDiscardSheetDismiss,
            onConfirm = viewModel::closeRosterEditSheet
        )
    }
}

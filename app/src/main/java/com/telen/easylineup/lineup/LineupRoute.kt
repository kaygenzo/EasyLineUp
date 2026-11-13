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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.LoadFailedState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.lineup.defense.AvailablePlayersSheet
import com.telen.easylineup.lineup.defense.DpFlexLinkSheet
import com.telen.easylineup.lineup.edition.RosterEditSheet
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.startActivityOrNotify
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * A lineup, fixed or [editable], as the one [Routes.Lineup] destination.
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
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    fun goBack(afterDelete: Boolean = false) {
        when (lineupExit(fromShortcut, afterDelete)) {
            LineupExit.DASHBOARD -> navController.popBackStack(Routes.Dashboard, false)
            LineupExit.TOURNAMENTS ->
                navController.popBackStack(Routes.Tournaments, false) ||
                    navController.popBackStack()
            LineupExit.PREVIOUS -> navController.popBackStack()
        }
    }

    fun shareLineup(event: LineupEvent.ShareReady) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(
                Intent.EXTRA_SUBJECT,
                resources.getString(R.string.share_lineup_subject, event.lineupName)
            )
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, event.pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivityOrNotify(Intent.createChooser(intent, null))
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

    LaunchedEffect(viewModel) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    LineupEvent.Saved -> goBack()
                    LineupEvent.SaveFailed -> Toast.makeText(
                        context,
                        R.string.error_lineup_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    LineupEvent.Deleted -> goBack(afterDelete = true)
                    LineupEvent.DeleteFailed -> Toast.makeText(
                        context,
                        R.string.error_when_deleting_lineup,
                        Toast.LENGTH_LONG
                    ).show()
                    LineupEvent.EditFailed -> Toast.makeText(
                        context,
                        R.string.error_lineup_edit_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    is LineupEvent.ShareReady -> shareLineup(event)
                    LineupEvent.ShareFailed -> Toast.makeText(
                        context,
                        R.string.error_lineup_share_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    LineupEvent.DpFlexIncomplete -> FirebaseAnalyticsUtils.missingDpFlex(context)
                }
            }
    }

    // Not while the load failed: the discard sheet is not shown then, back just leaves.
    BackHandler(enabled = editable && !state.loadFailed) {
        FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit_cancel_back_pressed")
        viewModel.onDiscardSheetRequested()
    }

    if (state.loadFailed) {
        LoadFailedState(onRetry = viewModel::onRetryLoad)
        return
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
        onSaveClick = viewModel::onSaveClick,
        onShareLineup = viewModel::onShareClick,
        onDeleteClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_delete")
            viewModel.onDeleteSheetRequested()
        },
        onEditClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit")
            navController.navigate(Routes.Lineup(lineupId = lineupId, editable = true))
        },
        onEditRosterClick = viewModel::onEditRosterClick,
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
        val dpFlexTitle = if (event.teamType == TeamType.SOFTBALL) {
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
            onConfirm = viewModel::onDpFlexLinked,
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
            onConfirm = viewModel::onDeleteConfirmed
        )
    }

    if (state.showRosterEditSheet) {
        RosterEditSheet(
            roster = state.rosterEditSummary,
            teamType = state.teamType,
            onDismiss = viewModel::onRosterEditDismissRequested,
            onPlayerToggled = viewModel::onRosterEditPlayerToggled,
            onNumberChanged = viewModel::onRosterEditNumberChanged,
            onClearAll = viewModel::onRosterEditClearAll,
            onDoneClick = viewModel::onRosterEditDone
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

/** Where leaving the Lineup screen lands. */
internal enum class LineupExit { DASHBOARD, TOURNAMENTS, PREVIOUS }

/**
 * Opened from a home-screen shortcut, the screen always returns to the dashboard it sits on.
 * Otherwise a deleted lineup has no read-only view left to go back to, so it returns to the
 * tournaments list (or, if that is not on the back stack, wherever it came from); anything else
 * - saving, discarding, the back button - returns to the previous screen, which after an edit
 * is that lineup's read-only view.
 */
internal fun lineupExit(fromShortcut: Boolean, afterDelete: Boolean): LineupExit = when {
    fromShortcut -> LineupExit.DASHBOARD
    afterDelete -> LineupExit.TOURNAMENTS
    else -> LineupExit.PREVIOUS
}

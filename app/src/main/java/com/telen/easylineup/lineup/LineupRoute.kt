/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
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
import com.telen.easylineup.views.LineupTypeface
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
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

const val FRAGMENT_DEFENSE_INDEX = 0
const val FRAGMENT_ATTACK_INDEX = 1

/**
 * Replaces `LineupFragment`/`LineupFragmentFixed`/`LineupFragmentEditable` - [editable]
 * collapses what used to be two separate Fragment subclasses (same base class, only
 * differing by a constructor boolean) into the one [Routes.Lineup] destination.
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
        viewModel.lineupId = lineupId
        viewModel.editable = editable
    }

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

    var lineupName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(FRAGMENT_DEFENSE_INDEX) }

    var defensePlayers by remember { mutableStateOf<List<PlayerWithPosition>>(emptyList()) }
    var defenseStrategy by remember { mutableStateOf(TeamStrategy.STANDARD) }
    var lineupMode by remember { mutableStateOf(MODE_DISABLED) }
    var teamType by remember { mutableStateOf(TeamType.UNKNOWN.id) }
    var extraHitters by remember { mutableStateOf(0) }

    var availablePlayersEvent by remember { mutableStateOf<ListAvailablePlayers?>(null) }
    var dpFlexEvent by remember { mutableStateOf<LinkDpFlex?>(null) }
    var deleteConfirmPlayer by remember { mutableStateOf<Player?>(null) }
    var infoMessageRes by remember { mutableStateOf<Int?>(null) }

    var batters by remember { mutableStateOf<List<BatterState>>(emptyList()) }
    var typeface by remember { mutableStateOf(LineupTypeface.NORMAL) }

    var showDiscardSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }

    var rosterEditLineup by remember { mutableStateOf<Lineup?>(null) }
    var rosterEditSummary by remember {
        mutableStateOf(TeamRosterSummary(Constants.STATUS_ALL, emptyList()))
    }
    var rosterEditOriginalSummary by remember { mutableStateOf<TeamRosterSummary?>(null) }
    var showRosterEditSheet by remember { mutableStateOf(false) }
    var showRosterEditDiscardSheet by remember { mutableStateOf(false) }

    fun closeRosterEditSheet() {
        showRosterEditSheet = false
        showRosterEditDiscardSheet = false
    }

    fun handleRosterEditDismiss() {
        if (rosterEditSummary != rosterEditOriginalSummary) {
            showRosterEditDiscardSheet = true
        } else {
            closeRosterEditSheet()
        }
    }

    // Click-triggered, one-shot Rx subscription (exportLineupToExternalStorage returns a
    // Single<Intent>) - relocated as-is from BaseFragment.launch(), not rewritten, per the
    // same accepted-interop-boundary rule as the screen-lifetime subscriptions elsewhere.
    var shareDisposable by remember { mutableStateOf<Disposable?>(null) }
    DisposableEffect(Unit) { onDispose { shareDisposable?.dispose() } }

    fun shareLineup(bitmap: Bitmap) {
        val views = mapOf(selectedTab to bitmap)
        shareDisposable?.dispose()
        shareDisposable = viewModel.exportLineupToExternalStorage(context, views)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { intent -> context.startActivity(Intent.createChooser(intent, "")) },
                { Timber.e(it) }
            )
    }

    fun handlePositionClicked(position: FieldPosition) {
        coroutineScope.launch {
            viewModel.onPlayerClicked(position)
                .onSuccess { event ->
                    when (event) {
                        is LinkDpFlex -> dpFlexEvent = event
                        is ListAvailablePlayers -> availablePlayersEvent = event
                        null -> infoMessageRes = R.string.players_list_empty
                    }
                }
                .onFailure { throwable ->
                    if (throwable is NeedAssignPitcherFirstException) {
                        FirebaseAnalyticsUtils.missingPitcher(context)
                        infoMessageRes = R.string.error_need_assign_pitcher_first_message
                    } else {
                        Timber.e(throwable)
                    }
                }
        }
    }

    LaunchedEffect(viewModel, editionViewModel) {
        viewModel.observeLineupName()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { lineupName = it }
            .launchIn(this)

        viewModel.observeLineupStrategy()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { defenseStrategy = it }
            .launchIn(this)

        viewModel.observeLineupMode()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { lineupMode = it }
            .launchIn(this)

        viewModel.observeLineup()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { extraHitters = it.extraHitters }
            .launchIn(this)

        viewModel.observeDefensePlayers()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            // PlayerWithPosition instances are mutated in place by the domain use cases and the
            // same backing list is re-emitted - snapshot each element via .copy() so Compose's
            // structural-equality state check actually sees a change instead of comparing the
            // same mutated objects against themselves.
            .onEach { defensePlayers = it.map { player -> player.copy() } }
            .launchIn(this)

        viewModel.observeBatters()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { batters = it }
            .launchIn(this)

        viewModel.observeLineupTypeface(context)
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { typeface = it }
            .launchIn(this)

        launch {
            viewModel.getTeamType()
                .onSuccess { teamType = it }
                .onFailure { Timber.e(it) }
        }

        launch {
            editionViewModel.events
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { event -> if (event == LineupUpdated) closeRosterEditSheet() }
        }
    }

    BackHandler(enabled = editable) {
        FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit_cancel_back_pressed")
        showDiscardSheet = true
    }

    LineupScreen(
        lineupName = lineupName,
        editable = editable,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onBackClick = { goBack() },
        onCancelClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit_cancel_cancel")
            showDiscardSheet = true
        },
        onSaveClick = {
            coroutineScope.launch {
                viewModel.save()
                    .onSuccess {
                        Timber.d("Successfully saved")
                        goBack()
                    }
                    .onFailure { Timber.e(it) }
            }
        },
        onShareCaptured = { bitmap -> shareLineup(bitmap) },
        onDeleteClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_delete")
            showDeleteSheet = true
        },
        onEditClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_lineup_edit")
            navController.navigate(
                Routes.Lineup(lineupId = viewModel.lineupId ?: 0, editable = true)
            )
        },
        onEditRosterClick = {
            coroutineScope.launch {
                editionViewModel.load(viewModel.lineupId ?: 0)
                    .onSuccess { (loadedLineup, roster) ->
                        rosterEditLineup = loadedLineup
                        rosterEditSummary = roster
                        rosterEditOriginalSummary = roster
                        showRosterEditSheet = true
                    }
                    .onFailure { Timber.e(it) }
            }
        },
        defenseStrategy = defenseStrategy,
        defensePlayers = defensePlayers,
        lineupMode = lineupMode,
        teamType = teamType,
        extraHitters = extraHitters,
        onPositionClicked = ::handlePositionClicked,
        onPlayersSwitched = { player1, player2 ->
            coroutineScope.launch {
                viewModel.switchPlayersPosition(player1, player2)
                    .onFailure { Timber.e("Cannot switch players: ${it.message}") }
            }
        },
        onPlayerReassigned = { player, newPosition ->
            coroutineScope.launch {
                viewModel.switchPlayersPosition(player, newPosition)
                    .onFailure { Timber.e("Cannot change player position: ${it.message}") }
            }
        },
        onPlayerSentToTrash = { player, _ -> viewModel.onDeletePosition(player) },
        onSubstituteLongClicked = { player -> deleteConfirmPlayer = player },
        onAddSubstitute = { handlePositionClicked(FieldPosition.SUBSTITUTE) },
        onLineupModeChanged = { enabled -> viewModel.onLineupModeChanged(enabled) },
        batters = batters,
        typeface = typeface,
        onReorder = { updated ->
            coroutineScope.launch {
                viewModel.onBattersChanged(updated)
                    .onFailure { Timber.e(it) }
            }
        }
    )

    availablePlayersEvent?.let { event ->
        AvailablePlayersSheet(
            players = event.players,
            lineupPlayers = defensePlayers,
            filterPosition = event.position,
            teamType = teamType,
            extraHitters = extraHitters,
            onPlayerSelected = { player ->
                viewModel.onPlayerSelected(player, event.position)
                availablePlayersEvent = null
            },
            onDismiss = { availablePlayersEvent = null }
        )
    }

    dpFlexEvent?.let { event ->
        DpFlexLinkSheet(
            initialDp = event.initialData.first?.toPlayer(),
            initialFlex = event.initialData.second?.toPlayer(),
            dpBattingOrder = event.initialData.first?.order,
            dpLocked = event.dpLocked,
            flexLocked = event.flexLocked,
            teamType = event.teamType,
            title = stringResource(event.title),
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
                        .onSuccess { dpFlexEvent = null }
                        .onFailure { throwable ->
                            if (throwable is NeedAssignBothPlayersException) {
                                Timber.w(throwable.message)
                                FirebaseAnalyticsUtils.missingDpFlex(context)
                                infoMessageRes = R.string.error_need_assign_both_players_message
                            } else {
                                Timber.e(throwable)
                            }
                        }
                }
            },
            onDismiss = { dpFlexEvent = null }
        )
    }

    deleteConfirmPlayer?.let { player ->
        ConfirmSheet(
            title = stringResource(R.string.dialog_delete_substitute_title, player.name),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = { deleteConfirmPlayer = null },
            onConfirm = { viewModel.onDeletePosition(player) }
        )
    }

    infoMessageRes?.let { messageRes ->
        AlertDialog(
            onDismissRequest = { infoMessageRes = null },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = { infoMessageRes = null }) {
                    Text(stringResource(android.R.string.ok))
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
            onConfirm = { goBack() }
        )
    }

    if (showDeleteSheet) {
        ConfirmSheet(
            title = stringResource(R.string.dialog_delete_lineup_title),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = { showDeleteSheet = false },
            onConfirm = {
                coroutineScope.launch {
                    viewModel.deleteLineup()
                        .onSuccess { goBack(showFixedView = false) }
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

    if (showRosterEditSheet) {
        RosterEditSheet(
            roster = rosterEditSummary,
            teamType = TeamType.getTypeById(teamType),
            onDismiss = { handleRosterEditDismiss() },
            onPlayerToggled = { position, checked ->
                rosterEditSummary = rosterEditSummary.withToggledPlayer(position, checked)
            },
            onNumberChanged = { position, number ->
                rosterEditSummary = rosterEditSummary.withNumberOverride(
                    position,
                    number,
                    rosterEditLineup?.id ?: 0L
                )
            },
            onClearAll = { rosterEditSummary = rosterEditSummary.withAllCleared() },
            onDoneClick = {
                rosterEditLineup?.let { editionViewModel.save(it, rosterEditSummary) }
            }
        )
    }

    if (showRosterEditDiscardSheet) {
        ConfirmSheet(
            title = stringResource(R.string.discard_change_title),
            message = stringResource(R.string.discard_change_message),
            confirmLabel = stringResource(R.string.action_discard),
            cancelLabel = stringResource(R.string.action_keep_editing),
            onDismiss = { showRosterEditDiscardSheet = false },
            onConfirm = { closeRosterEditSheet() }
        )
    }
}

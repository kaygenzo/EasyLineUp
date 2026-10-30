/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.lineup.LineupViewModel
import com.telen.easylineup.lineup.LinkDpFlex
import com.telen.easylineup.lineup.ListAvailablePlayers
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class DefenseFragment : BaseFragment("DefenseFragment") {
    private val viewModel by viewModels<LineupViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private var players by mutableStateOf<List<PlayerWithPosition>>(emptyList())
    private var strategy by mutableStateOf(TeamStrategy.STANDARD)
    private var lineupMode by mutableStateOf(MODE_DISABLED)
    private var teamType by mutableStateOf(TeamType.UNKNOWN.id)
    private var extraHitters by mutableStateOf(0)

    private var availablePlayersEvent by mutableStateOf<ListAvailablePlayers?>(null)
    private var dpFlexEvent by mutableStateOf<LinkDpFlex?>(null)
    private var deleteConfirmPlayer by mutableStateOf<Player?>(null)
    private var infoMessageRes by mutableStateOf<Int?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    DefenseScreen(
                        strategy = strategy,
                        players = players,
                        lineupMode = lineupMode,
                        teamType = teamType,
                        extraHitters = extraHitters,
                        editable = viewModel.editable,
                        onPositionClicked = ::handlePositionClicked,
                        onPlayersSwitched = { player1, player2 ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewModel.switchPlayersPosition(player1, player2)
                                    .onFailure { Timber.e("Cannot switch players: ${it.message}") }
                            }
                        },
                        onPlayerReassigned = { player, newPosition ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewModel.switchPlayersPosition(player, newPosition)
                                    .onFailure {
                                        Timber.e("Cannot change player position: ${it.message}")
                                    }
                            }
                        },
                        onPlayerSentToTrash = { player, _ -> viewModel.onDeletePosition(player) },
                        onSubstituteLongClicked = { player -> deleteConfirmPlayer = player },
                        onAddSubstitute = { handlePositionClicked(FieldPosition.SUBSTITUTE) },
                        onLineupModeChanged = { enabled -> viewModel.onLineupModeChanged(enabled) }
                    )

                    availablePlayersEvent?.let { event ->
                        AvailablePlayersSheet(
                            players = event.players,
                            lineupPlayers = players,
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
                                viewLifecycleOwner.lifecycleScope.launch {
                                    viewModel.linkDpAndFlex(dp, flex)
                                        .onSuccess { dpFlexEvent = null }
                                        .onFailure { throwable ->
                                            if (throwable is NeedAssignBothPlayersException) {
                                                Timber.w(throwable.message)
                                                activity?.let(FirebaseAnalyticsUtils::missingDpFlex)
                                                infoMessageRes =
                                                    R.string.error_need_assign_both_players_message
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
                            title = stringResource(
                                R.string.dialog_delete_substitute_title,
                                player.name
                            ),
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
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeLineupStrategy()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { strategy = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineupMode()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { lineupMode = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineup()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { extraHitters = it.extraHitters }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeDefensePlayers()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            // PlayerWithPosition instances are mutated in place by the domain use cases and the
            // same backing list is re-emitted - snapshot each element via .copy() so Compose's
            // structural-equality state check actually sees a change instead of comparing the
            // same mutated objects against themselves.
            .onEach { players = it.map { player -> player.copy() } }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTeamType()
                .onSuccess { teamType = it }
                .onFailure { Timber.e(it) }
        }
    }

    private fun handlePositionClicked(position: FieldPosition) {
        viewLifecycleOwner.lifecycleScope.launch {
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
                        activity?.let(FirebaseAnalyticsUtils::missingPitcher)
                        infoMessageRes = R.string.error_need_assign_pitcher_first_message
                    } else {
                        Timber.e(throwable)
                    }
                }
        }
    }
}

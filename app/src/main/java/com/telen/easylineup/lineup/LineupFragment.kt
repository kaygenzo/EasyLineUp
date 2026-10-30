/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.defense.AvailablePlayersSheet
import com.telen.easylineup.lineup.defense.DpFlexLinkSheet
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.LineupTypeface
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

const val FRAGMENT_DEFENSE_INDEX = 0
const val FRAGMENT_ATTACK_INDEX = 1

class LineupFragmentFixed : LineupFragment("LineupFragmentFixed", false)

class LineupFragmentEditable : LineupFragment("LineupFragmentEditable", true)

abstract class LineupFragment(
    fragmentName: String,
    private val isEditable: Boolean
) : BaseFragment(fragmentName) {
    private val viewModel by viewModels<LineupViewModel>()

    private var lineupName by mutableStateOf("")
    private var selectedTab by mutableStateOf(FRAGMENT_DEFENSE_INDEX)

    private var defensePlayers by mutableStateOf<List<PlayerWithPosition>>(emptyList())
    private var defenseStrategy by mutableStateOf(TeamStrategy.STANDARD)
    private var lineupMode by mutableStateOf(MODE_DISABLED)
    private var teamType by mutableStateOf(TeamType.UNKNOWN.id)
    private var extraHitters by mutableStateOf(0)

    private var availablePlayersEvent by mutableStateOf<ListAvailablePlayers?>(null)
    private var dpFlexEvent by mutableStateOf<LinkDpFlex?>(null)
    private var deleteConfirmPlayer by mutableStateOf<Player?>(null)
    private var infoMessageRes by mutableStateOf<Int?>(null)

    private var batters by mutableStateOf<List<BatterState>>(emptyList())
    private var typeface by mutableStateOf(LineupTypeface.NORMAL)

    private var showDiscardSheet by mutableStateOf(false)
    private var showDeleteSheet by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.lineupId = arguments?.getLong(Constants.LINEUP_ID, 0) ?: 0
        viewModel.editable = isEditable
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(isEditable) {
                override fun handleOnBackPressed() {
                    FirebaseAnalyticsUtils.onClick(
                        activity,
                        "click_lineup_edit_cancel_back_pressed"
                    )
                    showDiscardSheet = true
                }
            }
        )

        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    LineupScreen(
                        lineupName = lineupName,
                        editable = isEditable,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onBackClick = { goBack() },
                        onCancelClick = {
                            FirebaseAnalyticsUtils.onClick(
                                activity,
                                "click_lineup_edit_cancel_cancel"
                            )
                            showDiscardSheet = true
                        },
                        onSaveClick = {
                            viewLifecycleOwner.lifecycleScope.launch {
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
                            FirebaseAnalyticsUtils.onClick(activity, "click_lineup_delete")
                            showDeleteSheet = true
                        },
                        onEditClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_lineup_edit")
                            val extras = getArguments(lineupId = viewModel.lineupId ?: 0)
                            findNavController().navigate(
                                R.id.lineupFragmentEditable,
                                extras,
                                NavigationUtils().getOptions()
                            )
                        },
                        defenseStrategy = defenseStrategy,
                        defensePlayers = defensePlayers,
                        lineupMode = lineupMode,
                        teamType = teamType,
                        extraHitters = extraHitters,
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
                        onLineupModeChanged = { enabled -> viewModel.onLineupModeChanged(enabled) },
                        batters = batters,
                        typeface = typeface,
                        onReorder = { updated ->
                            viewLifecycleOwner.lifecycleScope.launch {
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
                            onConfirm = { deleteLineup() }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeLineupName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { lineupName = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineupStrategy()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { defenseStrategy = it }
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
            .onEach { defensePlayers = it.map { player -> player.copy() } }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeBatters()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { batters = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineupTypeface(requireContext())
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { typeface = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTeamType()
                .onSuccess { teamType = it }
                .onFailure { Timber.e(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        // This screen draws its own ScreenHeader - the shared HomeActivity toolbar (still used
        // by every other, not-yet-migrated screen) would otherwise show a second, redundant app
        // bar above it. Paired with onPause rather than onViewCreated/onDestroyView: during a
        // Navigation Component transaction (e.g. view mode -> edit mode), the outgoing
        // fragment's onDestroyView can fire after the incoming one's onViewCreated, which would
        // re-show the toolbar right after this screen just hid it.
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
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

    private fun deleteLineup() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteLineup()
                .onSuccess { goBack(showFixedView = false) }
                .onFailure {
                    Timber.e(it)
                    Toast.makeText(
                        activity,
                        R.string.error_when_deleting_lineup,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun shareLineup(bitmap: Bitmap) {
        activity?.let {
            val views = mapOf(selectedTab to bitmap)
            launch(viewModel.exportLineupToExternalStorage(it, views), { intent ->
                startActivity(Intent.createChooser(intent, ""))
            })
        }
    }

    private fun goBack(showFixedView: Boolean = true) {
        val isShortcut = arguments?.getBoolean(Constants.EXTRA_IS_FROM_SHORTCUT) ?: false
        if (isShortcut) {
            findNavController().popBackStack(R.id.navigation_home, false)
        } else {
            val success =
                !showFixedView && findNavController().popBackStack(R.id.navigation_lineups, false)
            if (!success) {
                findNavController().popBackStack()
            }
        }
    }

    companion object {
        fun getArguments(lineupId: Long): Bundle {
            val extras = Bundle()
            extras.putLong(Constants.LINEUP_ID, lineupId)
            return extras
        }
    }
}

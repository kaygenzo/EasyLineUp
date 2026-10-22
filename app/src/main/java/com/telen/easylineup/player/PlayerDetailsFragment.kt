/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Hosts a swipeable carousel of player profiles (the roster's full id list, starting on
 * whichever player was tapped) - the Compose replacement for the old
 * `PlayersDetailsContainerFragment` (`ViewPager2` + per-page `PlayerDetailsFragment`) and its
 * now-redundant toolbar menu (edit/delete already live in [PlayerProfileScreen]'s own header).
 */
class PlayerDetailsFragment : BaseFragment("PlayerDetailsFragment") {
    private val teamViewModel: TeamViewModel by viewModel()

    private var playerIds by mutableStateOf<List<Long>>(emptyList())
    private var showDeleteAction by mutableStateOf<(suspend () -> Result<Unit>)?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val initialPlayerId = arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    val ids = playerIds
                    if (ids.isNotEmpty()) {
                        val pagerState = rememberPagerState(
                            initialPage = remember(ids) {
                                ids.indexOf(initialPlayerId).coerceAtLeast(0)
                            }
                        ) { ids.size }
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            PlayerProfilePage(
                                playerId = ids[page],
                                onBackClick = { findNavController().navigateUp() },
                                onEditRequested = ::onEditClicked,
                                onDeleteRequested = { showDeleteAction = it }
                            )
                        }
                    }

                    showDeleteAction?.let { deleteAction ->
                        ConfirmSheet(
                            title = stringResource(R.string.dialog_delete_player_title),
                            message = stringResource(R.string.dialog_delete_cannot_undo_message),
                            confirmLabel = stringResource(R.string.action_delete),
                            cancelLabel = stringResource(R.string.cancel_button),
                            onDismiss = { showDeleteAction = null },
                            onConfirm = { deletePlayer(deleteAction) }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamViewModel.observePlayers()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { players -> playerIds = players.map { it.id } }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onEditClicked(playerId: Long) {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_details_edit")
        val extras = Bundle().apply { putLong(Constants.PLAYER_ID, playerId) }
        findNavController().navigate(
            R.id.playerEditFragment,
            extras,
            NavigationUtils().getOptions()
        )
    }

    private fun deletePlayer(deleteAction: suspend () -> Result<Unit>) {
        val activity = activity ?: return
        FirebaseAnalyticsUtils.onClick(activity, "click_player_details_delete")
        viewLifecycleOwner.lifecycleScope.launch {
            deleteAction()
                .onSuccess { findNavController().popBackStack(R.id.navigation_team, false) }
                .onFailure {
                    val message = "Something wrong happened: ${it.message}"
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                }
        }
    }
}

@Composable
private fun PlayerProfilePage(
    playerId: Long,
    onBackClick: () -> Unit,
    onEditRequested: (Long) -> Unit,
    onDeleteRequested: (suspend () -> Result<Unit>) -> Unit
) {
    val context = LocalContext.current
    val viewModel = koinViewModel<PlayerViewModel>(key = playerId.toString())
    remember(playerId) { viewModel.playerId = playerId }

    val name by remember(playerId) { viewModel.observePlayerName() }
        .collectAsStateWithLifecycle(initialValue = "")
    val shirtNumber by remember(playerId) { viewModel.observePlayerShirtNumber() }
        .collectAsStateWithLifecycle(initialValue = 0)
    val licenseNumber by remember(playerId) { viewModel.observePlayerLicenseNumber() }
        .collectAsStateWithLifecycle(initialValue = 0L)
    val imageUri by remember(playerId) {
        viewModel.observePlayerImage().map { it?.let(Uri::parse) }
    }.collectAsStateWithLifecycle(initialValue = null)
    val sex by remember(playerId) {
        viewModel.observePlayerSex().map { Sex.getById(it) }
    }.collectAsStateWithLifecycle(initialValue = Sex.UNKNOWN)
    val battingSide by remember(playerId) {
        viewModel.observePlayerBattingSide().map { PlayerSide.getSideByValue(it) }
    }.collectAsStateWithLifecycle(initialValue = null)
    val pitchingSide by remember(playerId) {
        viewModel.observePlayerPitchingSide().map { PlayerSide.getSideByValue(it) }
    }.collectAsStateWithLifecycle(initialValue = null)
    val email by remember(playerId) { viewModel.observePlayerEmail() }
        .collectAsStateWithLifecycle(initialValue = "")
    val phoneNumber by remember(playerId) { viewModel.observePlayerPhoneNumber() }
        .collectAsStateWithLifecycle(initialValue = "")
    val positionsData by remember(playerId) { viewModel.observeLineups() }
        .collectAsStateWithLifecycle(initialValue = emptyMap())
    val teamType by remember(playerId) {
        viewModel.observeTeamType().map { TeamType.getTypeById(it) }
    }.collectAsStateWithLifecycle(initialValue = TeamType.getTypeById(0))
    val strategy by remember(playerId) { viewModel.observeStrategy() }
        .collectAsStateWithLifecycle(initialValue = TeamStrategy.STANDARD)
    val strategyOptions by remember(playerId) { viewModel.observeStrategies(context) }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    PlayerProfileScreen(
        state = PlayerProfileState(
            name = name,
            shirtNumber = shirtNumber,
            imageUri = imageUri,
            sex = sex,
            battingSide = battingSide,
            pitchingSide = pitchingSide,
            licenseNumber = licenseNumber,
            email = email,
            phoneNumber = phoneNumber,
            positionsData = positionsData,
            teamType = teamType,
            strategy = strategy,
            strategyOptions = strategyOptions,
            selectedStrategyIndex = teamType.getStrategies().indexOf(strategy).coerceAtLeast(0)
        ),
        onStrategySelected = {
            FirebaseAnalyticsUtils.onClick(context, "click_player_details_strategy_selected")
            viewModel.onStrategySelected(it)
        },
        onBackClick = onBackClick,
        onEditClick = { onEditRequested(playerId) },
        onDeleteClick = { onDeleteRequested(viewModel::deletePlayer) }
    )
}

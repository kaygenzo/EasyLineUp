/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Hosts a swipeable carousel of player profiles (the roster's full id list, starting on
 * whichever player was tapped).
 */
@Composable
fun PlayerDetailsRoute(navController: NavController, initialPlayerId: Long) {
    val teamViewModel = koinViewModel<TeamViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val teamUiState by teamViewModel.uiState.collectAsStateWithLifecycle()

    // The player a delete was asked for. It stays set until that delete reports back, since
    // its pager page - and the collector it could have had - goes away with the player.
    var pendingDeletePlayerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDeleteSheet by rememberSaveable { mutableStateOf(false) }

    val ids = remember(teamUiState.players) { teamUiState.players.map { it.id } }
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
                onBackClick = { navController.navigateUp() },
                onEditRequested = { playerId ->
                    FirebaseAnalyticsUtils.onClick(context, "click_player_details_edit")
                    navController.navigate(Routes.PlayerEdit(playerId))
                },
                onDeleteRequested = { playerId ->
                    pendingDeletePlayerId = playerId
                    showDeleteSheet = true
                }
            )
        }
    }

    pendingDeletePlayerId?.let { playerId ->
        // Same key as the pager page's own PlayerViewModel: this is that page's instance, so
        // the delete runs in a scope that outlives both the sheet and the page.
        val deleteViewModel = koinViewModel<PlayerViewModel>(key = playerId.toString()) {
            parametersOf(playerId)
        }
        LaunchedEffect(deleteViewModel) {
            deleteViewModel.events
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        PlayerEvent.Deleted -> {
                            pendingDeletePlayerId = null
                            navController.popBackStack(Routes.Roster, false)
                        }
                        PlayerEvent.DeleteFailed -> {
                            pendingDeletePlayerId = null
                            Toast.makeText(
                                context,
                                R.string.error_player_delete_failed,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        PlayerEvent.Saved,
                        PlayerEvent.SaveRejected,
                        PlayerEvent.SaveFailed -> Unit
                    }
                }
        }

        if (showDeleteSheet) {
            ConfirmSheet(
                title = stringResource(R.string.dialog_delete_player_title),
                message = stringResource(R.string.dialog_delete_cannot_undo_message),
                confirmLabel = stringResource(R.string.action_delete),
                cancelLabel = stringResource(R.string.cancel_button),
                onDismiss = {
                    showDeleteSheet = false
                    pendingDeletePlayerId = null
                },
                onConfirm = {
                    FirebaseAnalyticsUtils.onClick(context, "click_player_details_delete")
                    showDeleteSheet = false
                    deleteViewModel.onDeleteConfirmed()
                }
            )
        }
    }
}

/**
 * One pager page: the route for a single player, with its own [PlayerViewModel]. No preview of
 * its own - like any Route it only wires state into [PlayerProfileScreen], which has them.
 */
@Composable
private fun PlayerProfilePage(
    playerId: Long,
    onBackClick: () -> Unit,
    onEditRequested: (Long) -> Unit,
    onDeleteRequested: (Long) -> Unit
) {
    val context = LocalContext.current
    val viewModel = koinViewModel<PlayerViewModel>(key = playerId.toString()) {
        parametersOf(playerId)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PlayerProfileScreen(
        state = PlayerProfileState(
            name = state.name,
            shirtNumber = state.shirtNumber,
            imageUri = state.imageUri,
            sex = state.sex,
            battingSide = state.battingSide,
            pitchingSide = state.pitchingSide,
            licenseNumber = state.licenseNumberText,
            email = state.email,
            phoneNumber = state.phone,
            positionsData = state.positionsData,
            teamType = state.teamType,
            strategy = state.strategy,
            selectedStrategyIndex = state.teamType.getStrategies().indexOf(state.strategy)
                .coerceAtLeast(0)
        ),
        onStrategySelected = { index ->
            FirebaseAnalyticsUtils.onClick(context, "click_player_details_strategy_selected")
            viewModel.onStrategySelected(state.teamType.getStrategies()[index])
        },
        onBackClick = onBackClick,
        onEditClick = { onEditRequested(playerId) },
        onDeleteClick = { onDeleteRequested(playerId) }
    )
}

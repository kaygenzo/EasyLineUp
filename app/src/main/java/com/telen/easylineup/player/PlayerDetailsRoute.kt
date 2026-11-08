/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

/**
 * Hosts a swipeable carousel of player profiles (the roster's full id list, starting on
 * whichever player was tapped) - replaces `PlayerDetailsFragment`/the old
 * `PlayersDetailsContainerFragment` (`ViewPager2` + per-page `PlayerDetailsFragment`).
 */
@Composable
fun PlayerDetailsRoute(navController: NavController, initialPlayerId: Long) {
    val teamViewModel = koinViewModel<TeamViewModel>()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val teamUiState by teamViewModel.uiState.collectAsStateWithLifecycle()

    var showDeleteAction by remember { mutableStateOf<(suspend () -> Result<Unit>)?>(null) }

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
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_player_details_delete")
                coroutineScope.launch {
                    deleteAction()
                        .onSuccess {
                            // See the matching comment in PlayerEditRoute's save flow - the
                            // explicit main-thread dispatch is needed for the same reason.
                            withContext(Dispatchers.Main) {
                                navController.popBackStack(Routes.Roster, false)
                            }
                        }
                        .onFailure {
                            val message = context.getString(
                                R.string.generic_error_with_detail,
                                it.message.orEmpty()
                            )
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                }
            }
        )
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PlayerProfileScreen(
        state = PlayerProfileState(
            name = state.name,
            shirtNumber = state.shirtNumber,
            imageUri = state.imageUri,
            sex = state.sex,
            battingSide = state.battingSide,
            pitchingSide = state.pitchingSide,
            licenseNumber = state.licenseNumber,
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
        onDeleteClick = { onDeleteRequested(viewModel::deletePlayer) }
    )
}

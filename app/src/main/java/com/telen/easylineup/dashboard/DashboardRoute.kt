/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.HomeViewModel
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.tiles.LastLineupData
import com.telen.easylineup.domain.model.tiles.MostUsedPlayerData
import com.telen.easylineup.domain.model.tiles.TeamSizeData
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.hideSoftKeyboard
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Replaces `DashboardFragment` - the `navigation_home` root tab. [homeViewModel] is
 * Activity-scoped (shared with the swap-team sheet hosted directly by
 * [com.telen.easylineup.HomeActivity]), so it is obtained once there and passed down
 * instead of being resolved per destination like every other screen's [koinViewModel].
 */
@Composable
fun DashboardRoute(navController: NavController, homeViewModel: HomeViewModel) {
    val viewModel = koinViewModel<DashboardViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var teamName by remember { mutableStateOf("") }
    var teamImageUri by remember { mutableStateOf<Uri?>(null) }
    var teamSize by remember { mutableStateOf<Int?>(null) }
    var teamType by remember { mutableStateOf(TeamType.UNKNOWN) }
    var mostUsedPlayer by remember { mutableStateOf<MostUsedPlayerUi?>(null) }
    var lastLineup by remember { mutableStateOf<LastLineupUi?>(null) }
    var shirtNumberQuery by remember { mutableStateOf("") }
    var shirtNumberResult by remember { mutableStateOf<String?>(null) }
    var shirtNumberHistory by remember { mutableStateOf<List<ShirtNumberEntry>>(emptyList()) }
    var showShirtNumberHistoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.registerTilesFlow()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { tiles ->
                // Each emission is a fresh team's tile set (possibly missing a type entirely
                // when that team has no games/lineups yet) - recompute every field from scratch
                // instead of only updating the types present, or a team switch would leave the
                // previous team's most-used-player/last-lineup/search state stuck on screen.
                val teamSizeData = tiles.firstOrNull { it.type == TileType.TEAM_SIZE.type }
                    ?.data as? TeamSizeData
                teamName = teamSizeData?.teamName ?: ""
                teamImageUri = teamSizeData?.teamImage?.let(Uri::parse)
                teamSize = teamSizeData?.size
                teamType = teamSizeData?.let { TeamType.getTypeById(it.teamType) }
                    ?: TeamType.UNKNOWN

                val mostUsedData = tiles.firstOrNull { it.type == TileType.MOST_USED_PLAYER.type }
                    ?.data as? MostUsedPlayerData
                mostUsedPlayer = mostUsedData?.let {
                    MostUsedPlayerUi(it.name.trim(), it.shirtNumber, it.matchPlayed)
                }

                val lastLineupData = tiles.firstOrNull { it.type == TileType.LAST_LINEUP.type }
                    ?.data as? LastLineupData
                lastLineup = lastLineupData?.let {
                    LastLineupUi(it.lineupId, it.name, it.eventTimeInMillis, it.tournamentName)
                }

                shirtNumberQuery = ""
                shirtNumberResult = null
                shirtNumberHistory = emptyList()
            }
    }

    DashboardScreen(
        state = DashboardState(
            teamName = teamName,
            teamImageUri = teamImageUri,
            teamSize = teamSize,
            teamType = teamType,
            mostUsedPlayer = mostUsedPlayer,
            lastLineup = lastLineup,
            shirtNumberQuery = shirtNumberQuery,
            shirtNumberResult = shirtNumberResult,
            shirtNumberHistory = shirtNumberHistory
        ),
        onSwitchTeamClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_dashboard_switch_team")
            homeViewModel.onSwapButtonClicked()
        },
        onRosterClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_dashboard_team_size")
            navController.navigate(Routes.Roster)
        },
        onLastLineupClick = { lineupId ->
            FirebaseAnalyticsUtils.onClick(context, "click_dashboard_last_lineup")
            navController.navigate(
                Routes.Lineup(lineupId = lineupId, editable = false, fromShortcut = true)
            )
        },
        onShirtNumberQueryChanged = {
            shirtNumberQuery = it.filter(Char::isDigit)
        },
        onShirtNumberSearch = {
            val number = shirtNumberQuery.toIntOrNull()
            if (number != null) {
                FirebaseAnalyticsUtils.onClick(context, "click_dashboard_search_player")
                (context as? Activity)?.hideSoftKeyboard()
                coroutineScope.launch {
                    viewModel.getShirtNumberHistory(number)
                        .onSuccess { history ->
                            shirtNumberHistory = history
                            shirtNumberResult = history.firstOrNull()?.playerName
                                ?: context.getString(R.string.generic_not_found)
                        }
                        .onFailure { Timber.e(it) }
                }
            }
        },
        onShirtNumberHistoryClick = {
            if (shirtNumberHistory.isNotEmpty()) {
                FirebaseAnalyticsUtils.onClick(context, "click_dashboard_number_history")
                showShirtNumberHistoryDialog = true
            }
        }
    )

    if (showShirtNumberHistoryDialog) {
        ShirtNumberHistorySheet(
            history = shirtNumberHistory,
            onDismiss = { showShirtNumberHistoryDialog = false }
        )
    }
}

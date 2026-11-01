/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.HomeViewModel
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.hideSoftKeyboard
import org.koin.androidx.compose.koinViewModel

/**
 * Replaces `DashboardFragment` - the `navigation_home` root tab. [homeViewModel] is
 * Activity-scoped (shared with the swap-team sheet hosted directly by
 * [com.telen.easylineup.HomeActivity]), so it is obtained once there and passed down
 * instead of being resolved per destination like every other screen's [koinViewModel].
 *
 * The Route itself holds no screen state - [DashboardViewModel.uiState] is the single source
 * of truth, collected here and forwarded straight into [DashboardScreen]. User actions are
 * forwarded back as plain method calls on the ViewModel; only Android-specific side effects
 * that don't belong in a ViewModel (navigation, analytics, hiding the keyboard) stay here.
 */
@Composable
fun DashboardRoute(navController: NavController, homeViewModel: HomeViewModel) {
    val viewModel = koinViewModel<DashboardViewModel>()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        state = uiState,
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
        onShirtNumberQueryChanged = viewModel::onShirtNumberQueryChanged,
        onShirtNumberSearch = {
            if (viewModel.onShirtNumberSearch()) {
                FirebaseAnalyticsUtils.onClick(context, "click_dashboard_search_player")
                (context as? Activity)?.hideSoftKeyboard()
            }
        },
        onShirtNumberHistoryClick = {
            if (viewModel.onShirtNumberHistoryClick()) {
                FirebaseAnalyticsUtils.onClick(context, "click_dashboard_number_history")
            }
        }
    )

    if (uiState.showShirtNumberHistoryDialog) {
        ShirtNumberHistorySheet(
            history = uiState.shirtNumberHistory,
            onDismiss = viewModel::onShirtNumberHistoryDismiss
        )
    }
}

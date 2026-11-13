/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.telen.easylineup.compose.BottomNavTab
import com.telen.easylineup.compose.DugoutBottomNav
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.navigation.AppNavHost
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.team.swap.SwapTeamSheet
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The main screen: [AppNavHost] (Jetpack Navigation Compose) hosts every destination, with the
 * bottom nav and the swap-team sheet as plain composables alongside it. There is no toolbar:
 * every destination draws its own `ScreenHeader`.
 */
class HomeActivity : BaseActivity() {
    private val viewModel by viewModel<HomeViewModel>()
    private var popToDashboard: (() -> Unit)? = null
    private val createTeam =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                popToDashboard?.invoke()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DugoutTheme {
                val navController = rememberNavController()
                popToDashboard = { navController.startOverOnDashboard() }
                val lifecycleOwner = LocalLifecycleOwner.current
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentTab = BottomNavTab.entries.find { tab ->
                    backStackEntry?.destination?.hasRoute(tab.route::class) == true
                }

                LaunchedEffect(viewModel) {
                    viewModel.events
                        .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                        .collect { event ->
                            when (event) {
                                UpdateCurrentTeamSuccess -> navController.startOverOnDashboard()

                                else -> {}
                            }
                        }
                }

                // Edge to edge: system bars, then whatever of a cutout they don't already cover
                // (landscape). The keyboard is left to adjustPan - see the manifest.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .displayCutoutPadding()
                ) {
                    AppNavHost(
                        navController = navController,
                        homeViewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                    currentTab?.let { tab ->
                        DugoutBottomNav(
                            selected = tab,
                            onSelect = { selected ->
                                if (selected != currentTab) {
                                    // Standard bottom-nav behavior: switching tabs never stacks a
                                    // tab on top of another (Dashboard > Roster > Dashboard used
                                    // to leave two Dashboards, so Back landed on the second one),
                                    // and each tab keeps its own depth, restored on reselect.
                                    navController.navigate(selected.route) {
                                        popUpTo<Routes.Dashboard> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }

                uiState.swapTeams?.let { teams ->
                    SwapTeamSheet(
                        teams = teams,
                        onDismiss = viewModel::onSwapTeamDismiss,
                        onTeamClick = { team ->
                            FirebaseAnalyticsUtils.onClick(
                                this@HomeActivity,
                                "click_team_swap_selection"
                            )
                            viewModel.updateCurrentTeam(team)
                            viewModel.onSwapTeamDismiss()
                        },
                        onCreateTeamClick = {
                            FirebaseAnalyticsUtils.onClick(
                                this@HomeActivity,
                                "click_team_swap_create"
                            )
                            onCreateTeamClick()
                            viewModel.onSwapTeamDismiss()
                        }
                    )
                }
            }
        }
    }

    /**
     * Back to the Dashboard after the current team changed. The other tabs' saved back stacks
     * belong to the team just left - a lineup or a player of it - so they are dropped rather
     * than restored on the next tab switch.
     */
    private fun NavController.startOverOnDashboard() {
        popBackStack(Routes.Dashboard, false)
        BottomNavTab.entries
            .filter { it != BottomNavTab.HOME }
            .forEach { tab -> clearBackStack(tab.route) }
    }

    private fun onCreateTeamClick() {
        FirebaseAnalyticsUtils.startTutorial(this, false)
        val intent = Intent(this, TeamCreationActivity::class.java)
        createTeam.launch(intent)
    }
}

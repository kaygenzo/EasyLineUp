/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.telen.easylineup.compose.BottomNavTab
import com.telen.easylineup.compose.DugoutBottomNav
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.navigation.AppNavHost
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.team.swap.SwapTeamSheet
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

/**
 * No layout XML and no Fragments back this screen - [AppNavHost] (Jetpack Navigation Compose)
 * replaces the old `NavHostFragment`/`app_navigation.xml` entirely, with the bottom nav and
 * swap-team sheet as plain composables alongside it. The shared native Toolbar this used to
 * install via `setSupportActionBar` is gone too: every destination already draws its own
 * `ScreenHeader`, so it was pure redundant chrome.
 */
class HomeActivity : BaseActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    private var popToDashboard: (() -> Unit)? = null
    private var showSwapTeamSheet by mutableStateOf(false)
    private var swapTeamTeams by mutableStateOf<List<Team>>(emptyList())
    private val createTeam =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                popToDashboard?.invoke()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            showSwapTeamSheet = it.getBoolean(KEY_SHOW_SWAP_TEAM_SHEET)
            @Suppress("UNCHECKED_CAST")
            swapTeamTeams = it.getSerializable(KEY_SWAP_TEAM_TEAMS) as? List<Team>
                ?: emptyList()
        }

        setContent {
            DugoutTheme {
                val navController = rememberNavController()
                popToDashboard = { navController.popBackStack(Routes.Dashboard, false) }

                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentTab = BottomNavTab.values().find { tab ->
                    backStackEntry?.destination?.hasRoute(tab.route::class) == true
                }

                DisposableEffect(viewModel) {
                    val disposable: Disposable = viewModel.observeEvents().subscribe({ event ->
                        when (event) {
                            UpdateCurrentTeamSuccess ->
                                navController.popBackStack(Routes.Dashboard, false)

                            is SwapButtonSuccess -> {
                                swapTeamTeams = event.teams
                                showSwapTeamSheet = true
                            }

                            else -> {}
                        }
                    }, { error -> Timber.e(error) })
                    onDispose { disposable.dispose() }
                }

                Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
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
                                    navController.navigate(selected.route) {
                                        popUpTo<Routes.Dashboard> { inclusive = false }
                                    }
                                }
                            }
                        )
                    }
                }

                if (showSwapTeamSheet) {
                    SwapTeamSheet(
                        teams = swapTeamTeams,
                        onDismiss = { showSwapTeamSheet = false },
                        onTeamClick = { team ->
                            FirebaseAnalyticsUtils.onClick(
                                this@HomeActivity,
                                "click_team_swap_selection"
                            )
                            viewModel.updateCurrentTeam(team)
                            showSwapTeamSheet = false
                        },
                        onCreateTeamClick = {
                            FirebaseAnalyticsUtils.onClick(
                                this@HomeActivity,
                                "click_team_swap_create"
                            )
                            onCreateTeamClick()
                            showSwapTeamSheet = false
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SHOW_SWAP_TEAM_SHEET, showSwapTeamSheet)
        outState.putSerializable(KEY_SWAP_TEAM_TEAMS, ArrayList(swapTeamTeams))
    }

    private fun onCreateTeamClick() {
        FirebaseAnalyticsUtils.startTutorial(this, false)
        val intent = Intent(this, TeamCreationActivity::class.java)
        createTeam.launch(intent)
    }

    private companion object {
        const val KEY_SHOW_SWAP_TEAM_SHEET = "key_show_swap_team_sheet"
        const val KEY_SWAP_TEAM_TEAMS = "key_swap_team_teams"
    }
}

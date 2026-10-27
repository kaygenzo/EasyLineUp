/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.telen.easylineup.compose.BottomNavTab
import com.telen.easylineup.compose.DugoutBottomNav
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.databinding.ActivityHomeBinding
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.team.swap.SwapTeamSheet
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import timber.log.Timber

class HomeActivity : BaseActivity() {
    private val viewModel by viewModels<HomeViewModel>()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.navigation_home,
            R.id.navigation_team,
            R.id.navigation_lineups,
            R.id.navigation_settings
        )
    )
    private var binding: ActivityHomeBinding? = null
    private var currentTab by mutableStateOf<BottomNavTab?>(null)
    private var showSwapTeamSheet by mutableStateOf(false)
    private var swapTeamTeams by mutableStateOf<List<Team>>(emptyList())
    private val createTeam =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                navController.popBackStack(R.id.navigation_home, false)
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
        binding = ActivityHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            setupActionBarWithNavController(navController, appBarConfiguration)

            val bottomNav: ComposeView = findViewById(R.id.bottomNavComposeView)
            bottomNav.setContent {
                DugoutTheme {
                    DugoutBottomNav(
                        selected = currentTab,
                        onSelect = { tab ->
                            if (tab.destinationId != navController.currentDestination?.id) {
                                navController.navigate(
                                    tab.destinationId,
                                    null,
                                    NavigationUtils().getOptionsWithPopDestination(
                                        R.id.navigation_home,
                                        false
                                    )
                                )
                            }
                        }
                    )

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

            navController.addOnDestinationChangedListener { _, destination, _ ->
                currentTab = BottomNavTab.values().find { it.destinationId == destination.id }
                bottomNav.visibility = if (currentTab != null) View.VISIBLE else View.GONE
            }

            val disposable = viewModel.observeEvents().subscribe({
                when (it) {
                    UpdateCurrentTeamSuccess -> {
                        navController.popBackStack(R.id.navigation_home, false)
                    }

                    is SwapButtonSuccess -> {
                        swapTeamTeams = it.teams
                        showSwapTeamSheet = true
                    }

                    else -> {}
                }
            }, {
                Timber.e(it)
            })

            disposables.add(disposable)
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
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

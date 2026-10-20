/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.HomeViewModel
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.tiles.LastLineupData
import com.telen.easylineup.domain.model.tiles.MostUsedPlayerData
import com.telen.easylineup.domain.model.tiles.TeamSizeData
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.utils.hideSoftKeyboard
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class DashboardFragment : BaseFragment("DashboardFragment") {
    private val viewModel by viewModel<DashboardViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()

    private var teamName by mutableStateOf("")
    private var teamImageUri by mutableStateOf<Uri?>(null)
    private var teamSize by mutableStateOf<Int?>(null)
    private var teamType by mutableStateOf(TeamType.UNKNOWN)
    private var mostUsedPlayer by mutableStateOf<MostUsedPlayerUi?>(null)
    private var lastLineup by mutableStateOf<LastLineupUi?>(null)
    private var shirtNumberQuery by mutableStateOf("")
    private var shirtNumberResult by mutableStateOf<String?>(null)
    private var shirtNumberHistory by mutableStateOf<List<ShirtNumberEntry>>(emptyList())
    private var showShirtNumberHistoryDialog by mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
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
                        onSwitchTeamClick = ::onSwitchTeamClicked,
                        onRosterClick = ::onRosterClicked,
                        onLastLineupClick = ::onLastLineupClicked,
                        onShirtNumberQueryChanged = {
                            shirtNumberQuery = it.filter(Char::isDigit)
                        },
                        onShirtNumberSearch = ::onShirtNumberSearch,
                        onShirtNumberHistoryClick = {
                            if (shirtNumberHistory.isNotEmpty()) {
                                FirebaseAnalyticsUtils.onClick(
                                    activity,
                                    "click_dashboard_number_history"
                                )
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
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registerTilesFlow()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { tiles ->
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
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onSwitchTeamClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_switch_team")
        homeViewModel.onSwapButtonClicked()
    }

    private fun onRosterClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_team_size")
        findNavController().navigate(R.id.navigation_team, null, NavigationUtils().getOptions())
    }

    private fun onLastLineupClicked(lineupId: Long) {
        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_last_lineup")
        val extras = LineupFragment.getArguments(lineupId).apply {
            putBoolean(Constants.EXTRA_IS_FROM_SHORTCUT, true)
        }
        findNavController().navigate(
            R.id.lineupFragmentFixed,
            extras,
            NavigationUtils().getOptions()
        )
    }

    private fun onShirtNumberSearch() {
        val number = shirtNumberQuery.toIntOrNull() ?: return
        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_search_player")
        hideSoftKeyboard()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getShirtNumberHistory(number)
                .onSuccess { history ->
                    shirtNumberHistory = history
                    shirtNumberResult = history.firstOrNull()?.playerName
                        ?: getString(R.string.generic_not_found)
                }
                .onFailure { Timber.e(it) }
        }
    }
}

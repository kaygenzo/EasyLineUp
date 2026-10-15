/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayersFragment : BaseFragment("TeamFragment") {
    private val viewModel: TeamViewModel by viewModel()

    private var players by mutableStateOf<List<RosterRowState>>(emptyList())
    private var sortType by mutableStateOf(TeamViewModel.SortType.ALPHA)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    RosterScreen(
                        state = RosterState(players = players, sortType = sortType),
                        onSortSelected = ::onSortSelected,
                        onPlayerClick = ::onPlayerSelected,
                        onAddPlayerClick = ::onAddPlayerClicked
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observePlayers()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { playerList -> players = playerList.map { it.toRosterRowState() } }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

    private fun onSortSelected(newSortType: TeamViewModel.SortType) {
        sortType = newSortType
        viewModel.setSortType(newSortType)
    }

    private fun onAddPlayerClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_create")
        findNavController().navigate(R.id.playerEditFragment, null, NavigationUtils().getOptions())
    }

    private fun onPlayerSelected(playerId: Long) {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_selected")
        val extras = Bundle()
        extras.putLong(Constants.PLAYER_ID, playerId)
        findNavController().navigate(
            R.id.playerDetailsFragment,
            extras,
            NavigationUtils().getOptions()
        )
    }

    private fun Player.toRosterRowState(): RosterRowState = RosterRowState(
        id = id,
        name = name,
        shirtNumber = shirtNumber,
        imageUri = image?.let { Uri.parse(it) },
        battingSide = PlayerSide.getSideByValue(batting),
        pitchingSide = PlayerSide.getSideByValue(pitching)
    )
}

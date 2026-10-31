/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TournamentStatisticsFragment : BaseFragment("TournamentStatisticsFragment") {
    private val viewModel: TournamentStatisticsViewModel by viewModel()

    private var tournament: Tournament? = null
    private var teamType: TeamType = TeamType.UNKNOWN
    private var playerStats by mutableStateOf<List<PlayerStatsRow>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(Constants.EXTRA_TOURNAMENT)?.let {
            tournament = it as Tournament
        }
        arguments?.getInt(Constants.EXTRA_TEAM_TYPE)?.let {
            teamType = TeamType.getTypeById(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    TournamentStatisticsScreen(
                        tournamentName = tournament?.name.orEmpty(),
                        playerStats = playerStats,
                        teamType = teamType,
                        onBackClick = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentTournament = tournament ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.load(currentTournament)
                .onSuccess { playerStats = it }
                .onFailure { Timber.e(it) }
        }
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.lineup.edition.LineupEditionFragment
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TournamentListFragment : BaseFragment("TournamentListFragment") {
    private val viewModel by viewModels<LineupViewModel>()
    private val teamViewModel: TeamViewModel by viewModel()

    private var teamName by mutableStateOf("")
    private var teamType by mutableStateOf(TeamType.UNKNOWN)
    private var tournamentItems by mutableStateOf<List<TournamentItem>>(emptyList())
    private var mapsByTournamentId by mutableStateOf<Map<Long, GeoLocation>>(emptyMap())
    private var searchQuery by mutableStateOf("")
    private var tournamentPendingDeletion by mutableStateOf<Tournament?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    TournamentListScreen(
                        state = TournamentListState(
                            teamName = teamName,
                            teamType = teamType,
                            items = tournamentItems,
                            mapsByTournamentId = mapsByTournamentId,
                            searchQuery = searchQuery
                        ),
                        onBackClick = { findNavController().navigateUp() },
                        onSearchQueryChanged = {
                            searchQuery = it
                            viewModel.setFilter(it)
                        },
                        onCreateLineupClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_create")
                            findNavController().navigate(
                                R.id.lineupCreationFragment,
                                null,
                                NavigationUtils().getOptions()
                            )
                        },
                        onLineupClick = ::onLineupClicked,
                        onEditLineupClick = ::onEditLineupClicked,
                        onStatsClick = ::onStatisticsTournamentClicked,
                        onDeleteClick = { tournamentPendingDeletion = it },
                        onMapClick = ::onMapClicked
                    )

                    tournamentPendingDeletion?.let { tournament ->
                        ConfirmSheet(
                            title = stringResource(
                                R.string.dialog_delete_tournament_title,
                                tournament.name
                            ),
                            message = stringResource(R.string.dialog_delete_cannot_undo_message),
                            confirmLabel = stringResource(R.string.action_delete),
                            cancelLabel = stringResource(R.string.cancel_button),
                            onDismiss = { tournamentPendingDeletion = null },
                            onConfirm = {
                                FirebaseAnalyticsUtils.onClick(
                                    activity,
                                    "click_tournaments_delete"
                                )
                                deleteTournament(tournament)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamViewModel.observeCurrentTeamName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.mapsFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { (tournament, mapInfo) ->
                mapInfo.location?.let { location ->
                    mapsByTournamentId = mapsByTournamentId + (tournament.id to location)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeCategorizedLineups()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { tournamentItems = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTeamType()
                .onSuccess { teamType = TeamType.getTypeById(it) }
                .onFailure { Timber.e(it) }
        }

        viewModel.setFilter("")
    }

    private fun onStatisticsTournamentClicked(tournament: Tournament) {
        FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_stats")
        val extras = Bundle()
        extras.putSerializable(Constants.EXTRA_TOURNAMENT, tournament)
        extras.putInt(Constants.EXTRA_TEAM_TYPE, teamType.id)
        findNavController().navigate(
            R.id.tournamentStatisticsTableFragment,
            extras,
            NavigationUtils().getOptions()
        )
    }

    private fun onEditLineupClicked(lineup: Lineup) {
        findNavController().navigate(
            R.id.lineupRosterFragment,
            LineupEditionFragment.getBundle(lineup.id),
            NavigationUtils().getOptions()
        )
    }

    private fun onMapClicked(location: GeoLocation) {
        val urlString = "geo:${location.latitude},${location.longitude}"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlString)))
    }

    private fun onLineupClicked(lineup: Lineup) {
        FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_lineup_selected")
        findNavController().navigate(
            R.id.lineupFragmentFixed,
            LineupFragment.getArguments(lineup.id),
            NavigationUtils().getOptions()
        )
    }

    private fun deleteTournament(tournament: Tournament) {
        val activity = activity ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteTournament(tournament)
                .onSuccess { viewModel.setFilter("") }
                .onFailure { throwable ->
                    Timber.e(throwable)
                    Toast.makeText(
                        activity,
                        "Something wrong happened: ${throwable.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}

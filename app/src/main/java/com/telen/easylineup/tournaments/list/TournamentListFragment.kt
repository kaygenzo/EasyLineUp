package com.telen.easylineup.tournaments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentListTournamentsBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.lineup.edition.LineupEditionFragment
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.utils.hideSoftKeyboard
import com.telen.easylineup.views.OnSearchBarListener
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LineupsScrollListener(private val view: FloatingActionButton) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0 && view.visibility == View.VISIBLE) {
            view.hide()
        } else if (dy < 0 && view.visibility != View.VISIBLE) {
            view.show()
        }
    }
}

class TournamentListFragment : BaseFragment("TournamentListFragment"), OnTournamentItemListener,
    OnSearchBarListener {

    private lateinit var tournamentsAdapter: TournamentsAdapter
    private val viewModel by viewModels<LineupViewModel>()
    private var binding: FragmentListTournamentsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournamentsAdapter = TournamentsAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentListTournamentsBinding.inflate(layoutInflater, container, false).apply {
            binding = this

            viewModel.mapsFlow
                .onEach { tournamentsAdapter.setMapToTournament(it.first, it.second) }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            recyclerView.apply {
                layoutManager = GridLayoutManager(context, 1)
                adapter = tournamentsAdapter

                //display or hide the fab when scrolling
                addOnScrollListener(LineupsScrollListener(fab))
            }

            fab.setOnClickListener {
                FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_create")
                findNavController().navigate(
                    R.id.lineupCreationFragment,
                    null,
                    NavigationUtils().getOptions()
                )
            }

            viewModel.observeCategorizedLineups().observe(viewLifecycleOwner) {
                tournamentsAdapter.setList(it)
            }

            launch(viewModel.getTeamType(), {
                tournamentsAdapter.setTeamType(it)
            })

            viewModel.setFilter("")

            materialSearchBar.onSearchBarListener = this@TournamentListFragment
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerView?.apply {
            adapter = null
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.materialSearchBar?.setIdle()
    }

    override fun onDeleteTournamentClicked(tournament: Tournament) {
        activity?.let {
            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_delete")
            val task: Completable = viewModel.deleteTournament(tournament)
                .doOnError { throwable ->
                    Toast.makeText(
                        activity,
                        "Something wrong happened: ${throwable.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .doOnComplete {
                    viewModel.setFilter("")
                }
            DialogFactory.getWarningTaskDialog(
                context = it,
                title = R.string.dialog_delete_tournament_title,
                titleArgs = arrayOf(tournament.name),
                message = R.string.dialog_delete_cannot_undo_message,
                task = task
            ).show()
        }
    }

    override fun onStatisticsTournamentClicked(teamType: TeamType, tournament: Tournament) {
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

    override fun onEditLineupClicked(lineup: Lineup) {
        findNavController().navigate(
            R.id.lineupRosterFragment,
            LineupEditionFragment.getBundle(lineup.id),
            NavigationUtils().getOptions()
        )
    }

    override fun onHeaderClicked() {

    }

    override fun onLineupClicked(lineup: Lineup) {
        activity?.let {
            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_lineup_selected")
            val extras = LineupFragment.getArguments(lineup.id)
            findNavController().navigate(
                R.id.lineupFragmentFixed,
                extras,
                NavigationUtils().getOptions()
            )
        }
    }

    override fun onSearchConfirmed(text: String?) {
        text?.let {
            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_search")
            viewModel.setFilter(it)
            hideSoftKeyboard()
        }
    }
}
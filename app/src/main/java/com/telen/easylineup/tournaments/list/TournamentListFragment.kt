package com.telen.easylineup.tournaments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.utils.hideSoftKeyboard
import com.telen.easylineup.views.OnSearchBarListener
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_list_tournaments.*
import kotlinx.android.synthetic.main.fragment_list_tournaments.view.*
import timber.log.Timber


class LineupsScrollListener(private val view: FloatingActionButton): RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0 && view.visibility == View.VISIBLE) {
            view.hide()
        } else if (dy < 0 && view.visibility != View.VISIBLE) {
            view.show()
        }
    }
}

class TournamentListFragment: BaseFragment("TournamentListFragment"), OnItemClickedListener, OnSearchBarListener {

    private lateinit var tournamentsAdapter: TournamentsAdapter
    private lateinit var viewModel: LineupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournamentsAdapter = TournamentsAdapter(this)
        viewModel =  ViewModelProviders.of(this).get(LineupViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_tournaments, container, false)

        view.recyclerView.apply {
            layoutManager = GridLayoutManager(view.context, 1)
            adapter = tournamentsAdapter

            //display or hide the fab when scrolling
            addOnScrollListener(LineupsScrollListener(view.fab))
        }

        view.fab.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_create")
            findNavController().navigate(R.id.lineupCreationFragment, null, NavigationUtils().getOptions())
        }

        viewModel.observeCategorizedLineups().observe(viewLifecycleOwner, Observer {
            tournamentsAdapter.setList(it)
        })

        val disposable = viewModel.getTeamType()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tournamentsAdapter.setTeamType(it)
                }, {
                    Timber.e(it)
                })
        this.disposables.add(disposable)

        viewModel.setFilter("")

        view.materialSearchBar.onSearchBarListener = this

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.recyclerView?.apply {
            adapter = null
        }
    }

    override fun onResume() {
        super.onResume()
        materialSearchBar.setIdle()
    }

    override fun onDeleteTournamentClicked(tournament: Tournament) {
        activity?.let {
            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_delete")
            val task: Completable = viewModel.deleteTournament(tournament)
                    .doOnError {throwable ->
                        Toast.makeText(activity, "Something wrong happened: ${throwable.message}", Toast.LENGTH_LONG).show()
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
        findNavController().navigate(R.id.tournamentStatisticsTableFragment, extras, NavigationUtils().getOptions())
    }

    override fun onHeaderClicked() {

    }

    override fun onLineupClicked(lineup: Lineup) {
        activity?.let {
            FirebaseAnalyticsUtils.onClick(activity, "click_tournaments_lineup_selected")
            val extras = LineupFragment.getArguments(lineup.id, lineup.name, TeamStrategy.getStrategyById(lineup.strategy), lineup.extraHitters)
            findNavController().navigate(R.id.lineupFragmentFixed, extras, NavigationUtils().getOptions())
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
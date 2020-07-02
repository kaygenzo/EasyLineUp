package com.telen.easylineup.lineup.list

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
import com.mancj.materialsearchbar.MaterialSearchBar
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.utils.hideSoftKeyboard
import io.reactivex.Completable
import kotlinx.android.synthetic.main.fragment_list_tournaments.view.*


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

class TournamentListFragment: BaseFragment(), OnItemClickedListener, MaterialSearchBar.OnSearchActionListener {

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

        view.searchBar.setOnSearchActionListener(this)

        view.fab.setOnClickListener {
            findNavController().navigate(R.id.lineupCreationFragment, null, NavigationUtils().getOptions())
        }

        viewModel.observeCategorizedLineups().observe(viewLifecycleOwner, Observer {
            tournamentsAdapter.setList(it)
        })

        viewModel.setFilter("")

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.recyclerView?.apply {
            adapter = null
        }
    }

    override fun onDeleteTournamentClicked(tournament: Tournament) {
        activity?.let {
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

    override fun onStatisticsTournamentClicked(tournament: Tournament) {
        val extras = Bundle()
        extras.putSerializable(Constants.EXTRA_TOURNAMENT, tournament)
        findNavController().navigate(R.id.tournamentStatisticsTableFragment, extras, NavigationUtils().getOptions())
    }

    override fun onHeaderClicked() {

    }

    override fun onLineupClicked(lineup: Lineup) {
        activity?.let {
            val extras = LineupFragment.getArguments(lineup.id, lineup.name)
            findNavController().navigate(R.id.lineupFragmentFixed, extras, NavigationUtils().getOptions())
        }
    }

    override fun onButtonClicked(buttonCode: Int) {
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        if(!enabled) {
            viewModel.setFilter("")
            hideSoftKeyboard()
        }
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        text?.let {
            viewModel.setFilter(it.toString())
            hideSoftKeyboard()
        }
    }
}
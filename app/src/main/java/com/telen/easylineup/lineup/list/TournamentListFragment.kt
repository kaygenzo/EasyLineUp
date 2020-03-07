package com.telen.easylineup.lineup.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.MaterialSearchBar
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.NavigationUtils
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_list_tournaments.view.*
import timber.log.Timber

class TournamentListFragment: Fragment(), OnItemClickedListener, MaterialSearchBar.OnSearchActionListener {

    private lateinit var tournamentsAdapter: TournamentsAdapter
    private lateinit var viewModel: LineupViewModel
    private val listTournaments: MutableList<Pair<Tournament, List<Lineup>>> = mutableListOf()
    private var loadTournamentsDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournamentsAdapter = TournamentsAdapter(this)
        viewModel =  ViewModelProviders.of(this).get(LineupViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_tournaments, container, false)

        val tournamentLayoutManager = GridLayoutManager(view.context, 1)

        view.recyclerView.apply {
            layoutManager = tournamentLayoutManager
            adapter = tournamentsAdapter

            //display or hide the fab when scrolling
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && view.fab.visibility == View.VISIBLE) {
                        view.fab.hide()
                    } else if (dy < 0 && view.fab.visibility != View.VISIBLE) {
                        view.fab.show()
                    }
                }
            })
        }

        view.searchBar.setOnSearchActionListener(this)
        view.searchBar.addTextChangeListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if((before > count + 1) && (count == 0)) {
//                    onFilterCallback?.onSearchConfirmed("")
//                }
            }

        })

        view.fab.setOnClickListener {
            findNavController().navigate(R.id.lineupCreationFragment, null, NavigationUtils().getOptions())
        }

        viewModel.registerFilterChanged().observe(this, Observer {
            loadTournaments(it)
        })

        viewModel.setFilter("")

        return view
    }

    private fun loadTournaments(filter: String) {
        loadTournamentsDisposable?.dispose()
        loadTournamentsDisposable = viewModel.getCategorizedLineups(filter).subscribe({
            listTournaments.clear()
            listTournaments.addAll(it)
            tournamentsAdapter.setList(listTournaments)
            tournamentsAdapter.notifyDataSetChanged()
        }, {
            Timber.e(it)
        })
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
            DialogFactory.getWarningDialog(it, it.getString(R.string.dialog_delete_tournament_title, tournament.name),
                    it.getString(R.string.dialog_delete_cannot_undo_message), task).show()
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
            val extras = Bundle()
            extras.putBoolean(Constants.EXTRA_EDITABLE, false)
            extras.putLong(Constants.LINEUP_ID, lineup.id)
            extras.putString(Constants.LINEUP_TITLE, lineup.name)
            findNavController().navigate(R.id.lineupFragment, extras, NavigationUtils().getOptions())
        }
    }

    override fun onButtonClicked(buttonCode: Int) {
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        if(!enabled)
            onSearch("")
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        text?.let {
            onSearch(it.toString())
        }
    }

    fun onSearch(text: String) {
        viewModel.setFilter(text)
    }
}
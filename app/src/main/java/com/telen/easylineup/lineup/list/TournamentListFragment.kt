package com.telen.easylineup.lineup.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.Tournament
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_list_tournaments.view.*

class TournamentListFragment: Fragment(), OnItemClickedListener {

    override fun onHeaderClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private lateinit var tournamentsAdapter: TournamentsAdapter
    private val listTournaments: MutableList<Pair<Tournament, List<Lineup>>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournamentsAdapter = TournamentsAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_tournaments, container, false)

        val tournamentLayoutManager = GridLayoutManager(view.context, 1)

        view.recyclerView.apply {
            layoutManager = tournamentLayoutManager
            adapter = tournamentsAdapter

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

        val categorizedViewModel =  ViewModelProviders.of(this).get(TournamentListViewModel::class.java)

        view.fab.setOnClickListener {
            findNavController().navigate(R.id.lineupCreationFragment, null, NavigationUtils().getOptions())
        }

        categorizedViewModel.getCategorizedLineups().observe(this, Observer { tournaments ->

            listTournaments.clear()
            tournaments.forEach { item ->
                val tournament = item.key
                val lineups = item.value
                listTournaments.add(Pair(tournament, lineups))
            }
            tournamentsAdapter.setList(listTournaments)
            tournamentsAdapter.notifyDataSetChanged()
        })

        return view
    }
}
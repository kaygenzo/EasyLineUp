package com.telen.easylineup.listLineup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.Tournament
import com.telen.easylineup.newLineup.LineupCreationDialog
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_list_lineup.view.*

class CategorizedListLineupFragment: Fragment() {

    private lateinit var sectionAdapter: SectionedRecyclerViewAdapter
    private lateinit var lineupViewModel: LineupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sectionAdapter = SectionedRecyclerViewAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_lineup, container, false)

        val tournamentViewModel =  ViewModelProviders.of(activity as AppCompatActivity).get(TournamentViewModel::class.java)
        lineupViewModel = ViewModelProviders.of(this).get(LineupViewModel::class.java)

        view.fab.setOnClickListener {
            val dialog = LineupCreationDialog()
            dialog.show(fragmentManager, "dialog")
        }

        val layoutMgr = LinearLayoutManager(activity as AppCompatActivity)

        view.recyclerView.apply {
            layoutManager = layoutMgr
            adapter = sectionAdapter
        }

        tournamentViewModel.tournaments.observe(this@CategorizedListLineupFragment, Observer { tournaments ->
            sectionAdapter.removeAllSections()
            tournaments.forEach { tournament ->
                val listLineup = mutableListOf<Lineup>()
                val section = CategorizedLineupAdapter(listLineup, tournament.name, object: OnHeaderClickedListener {
                    override fun onViewClicked() {
                        sectionAdapter.notifyDataSetChanged()
                    }
                })
                sectionAdapter.addSection(section)
                getLineupsFor(tournament, listLineup)
            }
            sectionAdapter.notifyDataSetChanged()
        })

        return view
    }

    private fun getLineupsFor(tournament: Tournament, list: MutableList<Lineup>) {
        lineupViewModel.getLineupsForTournament(tournament).observe(this@CategorizedListLineupFragment, Observer {
            list.clear()
            list.addAll(it)
            it.forEach { lineup ->
                getPlayersPositionsFor(lineup)
            }
        })
    }

    private fun getPlayersPositionsFor(lineup: Lineup) {
        lineupViewModel.getPlayerFieldPositionFor(lineup).observe(this@CategorizedListLineupFragment, Observer {
            lineup.playerFieldPosition.apply {
                clear()
                addAll(it)
            }
            sectionAdapter.notifyDataSetChanged()
        })
    }
}
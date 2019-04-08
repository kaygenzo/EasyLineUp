package com.telen.easylineup.history

import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.generic_list.view.*

class ListLineupFragment: Fragment() {

    private lateinit var lineupAdapter: ListLineupAdapter
    private lateinit var listLineups: MutableList<Lineup>
    private lateinit var lineupViewModel: LineupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listLineups = mutableListOf()
        lineupAdapter = ListLineupAdapter(listLineups)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.generic_list, container, false)
        view.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity as AppCompatActivity)
            adapter = lineupAdapter
        }

        lineupViewModel = ViewModelProviders.of(this).get(LineupViewModel::class.java)
        lineupViewModel.lineups.observe(this, Observer { lineups ->
            listLineups.apply {
                clear()
                addAll(lineups)
            }
            updateAdapter(lineups)
        })

        return view
    }

    fun updateAdapter(lineups: List<Lineup>) {
        lineups.forEach { lineup ->
            lineupViewModel.getPlayerFieldPositionFor(lineup).observe(this, Observer { fieldPositions ->
                lineup.playerFieldPosition.apply {
                    clear()
                    addAll(fieldPositions)
                }
                lineupAdapter.notifyDataSetChanged()
            })
        }
    }
}
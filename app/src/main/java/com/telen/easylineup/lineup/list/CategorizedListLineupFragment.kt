package com.telen.easylineup.lineup.list

import android.content.Intent
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
import com.telen.easylineup.lineup.LineupActivity
import com.telen.easylineup.lineup.create.LineupCreationDialog
import com.telen.easylineup.utils.Constants
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_list_lineup.view.*

class CategorizedListLineupFragment: Fragment() {

    private lateinit var sectionAdapter: SectionedRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sectionAdapter = SectionedRecyclerViewAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_lineup, container, false)

        val categorizedViewModel =  ViewModelProviders.of(this).get(CategorizedLineupsViewModel::class.java)

        view.fab.setOnClickListener {
            val dialog = LineupCreationDialog()
            dialog.show(fragmentManager, "dialog")
        }

        val layoutMgr = LinearLayoutManager(activity as AppCompatActivity)

        view.recyclerView.apply {
            layoutManager = layoutMgr
            adapter = sectionAdapter
        }

        categorizedViewModel.getCategorizedLineups().observe(this, Observer { tournaments ->
            sectionAdapter.removeAllSections()
            tournaments.forEach { item ->
                val tournament = item.key
                val lineups = item.value
                val section = CategorizedLineupAdapter(tournament, lineups, object : OnItemClickedListener {
                    override fun onLineupClicked(lineup: Lineup) {
                        activity?.let {
                            val intent = Intent(activity, LineupActivity::class.java)
                            intent.putExtra(Constants.EXTRA_EDITABLE, false)
                            intent.putExtra(Constants.LINEUP_ID, lineup.id)
                            intent.putExtra(Constants.LINEUP_TITLE, lineup.name)
                            startActivity(intent)
                        }
                    }

                    override fun onHeaderClicked() {
                        sectionAdapter.notifyDataSetChanged()
                    }
                })
                sectionAdapter.addSection(section)
            }
            sectionAdapter.notifyDataSetChanged()
        })

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_lineups)

        return view
    }
}
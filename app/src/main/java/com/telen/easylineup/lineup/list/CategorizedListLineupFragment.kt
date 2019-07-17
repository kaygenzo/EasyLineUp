package com.telen.easylineup.lineup.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.lineup.create.LineupCreationDialog
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
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
            fragmentManager?.let {
                dialog.show(it, "dialog")
            }
        }

        val layoutMgr = GridLayoutManager(activity as AppCompatActivity, resources.getInteger(R.integer.lineup_list_column_count))
        layoutMgr.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (sectionAdapter.getSectionItemViewType(position)) {
                    SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER -> resources.getInteger(R.integer.lineup_list_column_count)
                    else ->1
                }
            }
        }

        view.recyclerView.apply {
            layoutManager = layoutMgr
            adapter = sectionAdapter
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

        categorizedViewModel.getCategorizedLineups().observe(this, Observer { tournaments ->
            sectionAdapter.removeAllSections()
            tournaments.forEach { item ->
                val tournament = item.key
                val lineups = item.value
                val section = CategorizedLineupAdapter(tournament, lineups, object : OnItemClickedListener {
                    override fun onLineupClicked(lineup: Lineup) {
                        activity?.let {
                            val extras = Bundle()
                            extras.putBoolean(Constants.EXTRA_EDITABLE, false)
                            extras.putLong(Constants.LINEUP_ID, lineup.id)
                            extras.putString(Constants.LINEUP_TITLE, lineup.name)
                            findNavController().navigate(R.id.lineupFragment, extras, NavigationUtils().getOptions())
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

        return view
    }
}
package com.telen.easylineup.currentLineup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.*
import com.telen.easylineup.dashboard.models.ITileData
import com.telen.easylineup.dashboard.models.LastLineupData
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_dashboard.view.*

class DashboardFragment: Fragment(), TileClickListener {

    private lateinit var tileAdapter: DashboardTileAdapter
    private lateinit var dashboardViewModel: DashboardViewModel
    private val tileList = mutableListOf<ITileData>()
    private lateinit var dataObserver: Observer<List<ITileData>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tileAdapter = DashboardTileAdapter(tileList, this)
        dashboardViewModel = ViewModelProviders.of(this)[DashboardViewModel::class.java]
        dataObserver = Observer {
            tileList.clear()
            tileList.addAll(it)
            tileAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val columnCount = resources.getInteger(R.integer.dashboard_tile_count)

        val gridLayoutManager = GridLayoutManager(context, columnCount)
        gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if(tileList[position] is LastLineupData) {
                    columnCount
                } else {
                    1
                }
            }
        }

        view.tileRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = tileAdapter
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.registerTilesLiveData().observe(this, dataObserver)
        dashboardViewModel.registerTeamChange().observe(this, Observer {
            dashboardViewModel.loadTiles()
        })
    }

    override fun onTileClicked(type: Int) {
        when(type) {
            Constants.TYPE_TEAM_SIZE -> {
                findNavController().navigate(R.id.navigation_team, null, NavigationUtils().getOptions())
            }
            else -> {

            }
        }
    }
}
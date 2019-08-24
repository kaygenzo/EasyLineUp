package com.telen.easylineup.currentLineup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.DashboardTileAdapter
import com.telen.easylineup.dashboard.DashboardViewModel
import com.telen.easylineup.dashboard.models.ITileData
import com.telen.easylineup.dashboard.models.LastLineupData
import kotlinx.android.synthetic.main.fragment_dashboard.view.*

class DashboardFragment: Fragment() {

    lateinit var tileAdapter: DashboardTileAdapter
    lateinit var dashboardViewModel: DashboardViewModel
    private val tileList = mutableListOf<ITileData>()
    private lateinit var dataObserver: Observer<List<ITileData>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tileAdapter = DashboardTileAdapter(tileList)
        dashboardViewModel = ViewModelProviders.of(this)[DashboardViewModel::class.java]
        dataObserver = Observer {
            tileList.clear()
            tileList.addAll(it)
            tileAdapter.notifyDataSetChanged()
        }
        dashboardViewModel.registerTilesLiveData().observeForever(dataObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val columnCount = 2

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
        dashboardViewModel.loadTiles()
    }
}
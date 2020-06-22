package com.telen.easylineup.dashboard

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
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_ID
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_NAME
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

        view.tileRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = tileAdapter
        }

        dashboardViewModel.registerTilesLiveData().observe(viewLifecycleOwner, dataObserver)
        dashboardViewModel.registerTeamChange().observe(viewLifecycleOwner, Observer {
            dashboardViewModel.loadTiles()
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.tileRecyclerView?.apply {
            adapter = null
        }
    }

    override fun onTileClicked(type: Int, data: ITileData) {
        when(type) {
            Constants.TYPE_TEAM_SIZE -> {
                findNavController().navigate(R.id.navigation_team, null, NavigationUtils().getOptions())
            }
            Constants.TYPE_LAST_LINEUP -> {
                val extras = Bundle()
                val lineupID = data.getData()[KEY_LINEUP_ID] as? Long ?: 0L
                val lineupName = data.getData()[KEY_LINEUP_NAME] as? String ?: ""
                extras.putLong(Constants.LINEUP_ID, lineupID)
                extras.putString(Constants.LINEUP_TITLE, lineupName)
                findNavController().navigate(R.id.lineupFragmentFixed, extras, NavigationUtils().getOptions())
            }
            else -> {

            }
        }
    }
}
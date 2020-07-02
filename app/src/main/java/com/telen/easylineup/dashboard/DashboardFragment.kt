package com.telen.easylineup.dashboard

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_ID
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_NAME
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import timber.log.Timber

class DashboardFragment: BaseFragment(), TileClickListener, ActionMode.Callback {

    private lateinit var dashboardViewModel: DashboardViewModel
    private val tileList = mutableListOf<DashboardTile>()
    private lateinit var dataObserver: Observer<List<DashboardTile>>

    private lateinit var tileAdapter: DashboardTileAdapter
    private lateinit var itemTouchedCallback: DashboardTileTouchCallback
    private lateinit var itemTouchedHelper: ItemTouchHelper
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardViewModel = ViewModelProviders.of(this)[DashboardViewModel::class.java]
        dataObserver = Observer {
            tileList.clear()
            tileList.addAll(it)
            tileAdapter.notifyDataSetChanged()
        }
        tileAdapter = DashboardTileAdapter(tileList, this)
        itemTouchedCallback = DashboardTileTouchCallback(tileAdapter)
        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)
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
                val lineupID = data.getData()[KEY_LINEUP_ID] as? Long ?: 0L
                val lineupName = data.getData()[KEY_LINEUP_NAME] as? String ?: ""
                val extras = LineupFragment.getArguments(lineupID, lineupName)
                findNavController().navigate(R.id.lineupFragmentFixed, extras, NavigationUtils().getOptions())
            }
            else -> {

            }
        }
    }

    override fun onTileLongClicked(type: Int) {
        activity?.let { activity ->
            val inEditMode = tileAdapter.inEditMode
            if (!inEditMode) {
                actionMode = (activity as AppCompatActivity).startSupportActionMode(this)
                actionMode?.title = getString(R.string.dashboard_edit_mode)
            }
        }
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        tileAdapter.inEditMode = true
        tileAdapter.notifyDataSetChanged()
        itemTouchedHelper.attachToRecyclerView(view?.tileRecyclerView)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        tileAdapter.inEditMode = false
        tileAdapter.notifyDataSetChanged()
        itemTouchedHelper.attachToRecyclerView(null)
        val disposable = dashboardViewModel.saveTiles(tileList)
                .subscribe({
                    activity?.run {
                        if(BuildConfig.DEBUG)
                            Toast.makeText(this, "Save dashboard success", Toast.LENGTH_SHORT).show()
                    }
                }, {
                    Timber.e(it)
                })
        this.disposables.add(disposable)
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }
}
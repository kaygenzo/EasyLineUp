package com.telen.easylineup.team.details

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.PositionWithLineup
import com.telen.easylineup.lineup.list.ListLineupAdapter
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_player_details.view.*

class PlayerDetailsFragment: Fragment() {

    private lateinit var lineupsAdapter: ListLineupAdapter
    private val listLineup: MutableList<PositionWithLineup> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_details, container, false)

        lineupsAdapter = ListLineupAdapter(listLineup)

        val recyclerView = view.playerPositions
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = lineupsAdapter
        }

        val playerID = arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0

        val playerDetailsViewModel = ViewModelProviders.of(this).get(PlayerDetailsViewModel::class.java)
        playerDetailsViewModel.getAllLineupsForPlayer(playerID).observe(this, Observer { positions ->
            listLineup.clear()
            listLineup.addAll(positions)
            lineupsAdapter.notifyDataSetChanged()

            val chartData: MutableMap<FieldPosition, Int> = mutableMapOf()
            positions.forEach { position ->
                val fieldPosition = FieldPosition.getFieldPosition(position.position)
                fieldPosition?.let { element ->
                    chartData[element] = chartData[element]?.let { it + 1 } ?: 1
                }
            }
            view.positionsChart.setData(chartData)
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.player_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_edit -> {
                val extras = Bundle()
                extras.putLong(Constants.PLAYER_ID, arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0)
                extras.putLong(Constants.TEAM_ID, arguments?.getLong(Constants.TEAM_ID, 0) ?: 0)
                findNavController().navigate(R.id.playerEditFragment, extras, NavigationUtils().getOptions())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
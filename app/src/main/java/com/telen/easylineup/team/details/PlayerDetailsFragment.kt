package com.telen.easylineup.team.details

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.PositionWithLineup
import com.telen.easylineup.lineup.list.ListLineupAdapter
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_player_details.view.*

class PlayerDetailsFragment: Fragment() {

    private lateinit var lineupsAdapter: ListLineupAdapter
    private lateinit var playerDetailsViewModel: PlayerDetailsViewModel
    private val listLineup: MutableList<PositionWithLineup> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        playerDetailsViewModel = ViewModelProviders.of(this).get(PlayerDetailsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_details, container, false)

        lineupsAdapter = ListLineupAdapter(listLineup)

        val recyclerView = view.playerPositions
        recyclerView.apply {
            layoutManager = GridLayoutManager(activity, resources.getInteger(R.integer.player_lineup_list_column_count))
            adapter = lineupsAdapter
        }

        val playerID = arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
        playerDetailsViewModel.playerID = playerID

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
            R.id.action_delete -> {
                askUserConsentForDelete()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askUserConsentForDelete() {
        AlertDialog.Builder(activity as AppCompatActivity)
                .setCancelable(true)
                .setTitle(R.string.dialog_delete_player_title)
                .setMessage(R.string.dialog_delete_cannot_undo_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    playerDetailsViewModel.deletePlayer()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                findNavController().popBackStack(R.id.navigation_team, false)
                            }, {
                                Toast.makeText(activity, "Something wrong happened: ${it.message}", Toast.LENGTH_LONG).show()
                            })
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
    }

}
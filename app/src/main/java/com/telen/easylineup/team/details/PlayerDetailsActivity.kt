package com.telen.easylineup.team.details

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.PositionWithLineup
import com.telen.easylineup.lineup.list.ListLineupAdapter
import com.telen.easylineup.team.createPlayer.CreationPlayerDialog
import com.telen.easylineup.team.createPlayer.PlayerEditActivity
import com.telen.easylineup.utils.Constants
import kotlinx.android.synthetic.main.activity_player_details.*

class PlayerDetailsActivity: AppCompatActivity() {

    private lateinit var lineupsAdapter: ListLineupAdapter
    private val listLineup: MutableList<PositionWithLineup> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_player_details)

        lineupsAdapter = ListLineupAdapter(listLineup)

        val recyclerView = playerPositions
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlayerDetailsActivity)
            adapter = lineupsAdapter
        }

        val playerID = intent.getLongExtra(Constants.PLAYER_ID, 0)
        val teamID = intent.getLongExtra(Constants.TEAM_ID, 0)

        val playerDetailsViewModel = ViewModelProviders.of(this).get(PlayerDetailsViewModel::class.java)
        playerDetailsViewModel.getAllLineupsForPlayer(playerID).observe(this, Observer { positions ->
            listLineup.clear()
            listLineup.addAll(positions)
            lineupsAdapter.notifyDataSetChanged()

            val chartData: MutableMap<FieldPosition, Int> = mutableMapOf()
            positions.forEach { position ->
                val fieldPosition = FieldPosition.getFieldPosition(position.position)
                fieldPosition?.let { element ->
                    chartData[element] = if(chartData[element]!=null) {
                        chartData[element]!! + 1
                    }
                    else {
                        1
                    }
                }
            }
            positionsChart.setData(chartData)
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.player_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_edit -> {
                val intent = Intent(this, PlayerEditActivity::class.java)
                val playerID = getIntent().getLongExtra(Constants.PLAYER_ID, 0)
                val teamID = getIntent().getLongExtra(Constants.TEAM_ID, 0)
                intent.putExtra(Constants.PLAYER_ID, playerID)
                intent.putExtra(Constants.TEAM_ID, teamID)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
package com.telen.easylineup.team.details

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.data.PositionWithLineup
import com.telen.easylineup.listLineup.ListLineupAdapter
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

        val playerDetailsViewModel = ViewModelProviders.of(this).get(PlayerDetailsViewModel::class.java)
        playerDetailsViewModel.getAllLineupsForPlayer(playerID).observe(this, Observer { positions ->
            listLineup.clear()
            listLineup.addAll(positions)
            lineupsAdapter.notifyDataSetChanged()
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
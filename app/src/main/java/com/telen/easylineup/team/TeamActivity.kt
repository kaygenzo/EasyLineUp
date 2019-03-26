package com.telen.easylineup.team

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.Team
import kotlinx.android.synthetic.main.team_list_players.*

class TeamActivity: AppCompatActivity() {

    private lateinit var playersAdapter: TeamAdapter
    private lateinit var players: MutableList<Player>
    private lateinit var viewModel: TeamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.team_list_players)
        players = mutableListOf()
        playersAdapter = TeamAdapter(players)

        teamPlayersRecyclerView.apply {
            layoutManager = GridLayoutManager(this@TeamActivity, 2)
            adapter = playersAdapter
        }

        viewModel = ViewModelProviders.of(this).get(TeamViewModel::class.java)
        viewModel.team.observe(this, Observer {team ->
            players.apply {
                clear()
                addAll(team.players)
            }
            playersAdapter.notifyDataSetChanged()
        })

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
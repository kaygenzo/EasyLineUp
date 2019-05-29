package com.telen.easylineup.team

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.team.createPlayer.CreationPlayerDialog
import com.telen.easylineup.team.details.PlayerDetailsActivity
import com.telen.easylineup.utils.Constants
import kotlinx.android.synthetic.main.fragment_player_list.view.*

class TeamFragment: Fragment(), OnPlayerClickListener {

    private lateinit var playersAdapter: TeamAdapter
    private lateinit var players: MutableList<Player>
    private lateinit var viewModel: TeamViewModel
    private var teamID: Long? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        players = mutableListOf()
        playersAdapter = TeamAdapter(players, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_list, container, false)
        view.teamPlayersRecyclerView.apply {
            layoutManager = GridLayoutManager(activity as AppCompatActivity, 2)
            adapter = playersAdapter
        }

        viewModel = ViewModelProviders.of(this).get(TeamViewModel::class.java)
        viewModel.teams.observe(this, Observer { teams ->
            val team = teams.first()

            teamID = team.id

            viewModel.getPlayersForTeam(team.id).observe(this@TeamFragment, Observer { playerList ->
                players.apply {
                    clear()
                    addAll(playerList)
                }
                playersAdapter.notifyDataSetChanged()
            })

            view.fab.setOnClickListener {
                context?.let {
                    val fragment = CreationPlayerDialog()
                    val bundle = Bundle()
                    bundle.putLong(Constants.TEAM_ID, team.id)
                    fragment.arguments = bundle
                    fragment.show(fragmentManager, "dialog")
                }
            }
        })

        return view
    }

    override fun onPlayerSelected(player: Player) {
        val intent = Intent(activity, PlayerDetailsActivity::class.java)
        intent.putExtra(Constants.PLAYER_ID, player.id)
        intent.putExtra(Constants.TEAM_ID, teamID)
        startActivity(intent)
    }
}
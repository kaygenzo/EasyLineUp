package com.telen.easylineup.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_player_list.view.*

class TeamFragment: Fragment(), OnPlayerClickListener {

    private lateinit var playersAdapter: TeamAdapter
    private lateinit var players: MutableList<Player>
    private lateinit var viewModel: TeamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        players = mutableListOf()
        val clickable = arguments?.getBoolean(Constants.EXTRA_CLICKABLE) ?: true
        playersAdapter = TeamAdapter(players, when(clickable) {
            true -> this
            false -> null
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_list, container, false)
        view.teamPlayersRecyclerView.apply {
            layoutManager = GridLayoutManager(activity as AppCompatActivity, resources.getInteger(R.integer.player_list_column_count))
            adapter = playersAdapter
        }

        viewModel = ViewModelProviders.of(this).get(TeamViewModel::class.java)
        viewModel.getPlayers().observe(this@TeamFragment, Observer { playerList ->
            players.apply {
                clear()
                addAll(playerList)
            }
            playersAdapter.notifyDataSetChanged()

            view.fab.setOnClickListener {
                context?.let {
                    val bundle = Bundle()
                    bundle.putLong(Constants.TEAM_ID, viewModel.teamID ?: 0)
                    findNavController().navigate(R.id.playerEditFragment, bundle, NavigationUtils().getOptions())
                }
            }
        })

        return view
    }

    override fun onPlayerSelected(player: Player) {
        viewModel.teamID?.let {
            val extras = Bundle()
            extras.putLong(Constants.PLAYER_ID, player.id)
            extras.putLong(Constants.TEAM_ID, it)
            findNavController().navigate(R.id.playerDetailsFragment, extras, NavigationUtils().getOptions())
        }
    }
}
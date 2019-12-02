package com.telen.easylineup.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_player_list.view.*
import timber.log.Timber

class TeamFragment: Fragment(), OnPlayerClickListener {

    private lateinit var playersAdapter: TeamAdapter
    private lateinit var players: MutableList<Player>
    private lateinit var viewModel: TeamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        players = mutableListOf()
        val clickable = arguments?.getBoolean(Constants.EXTRA_CLICKABLE) ?: true
        activity?.let {
            playersAdapter = TeamAdapter(it, players, when(clickable) {
                true -> this
                false -> null
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_list, container, false)
        view.teamPlayersRecyclerView.apply {
            layoutManager = GridLayoutManager(activity as AppCompatActivity, resources.getInteger(R.integer.player_list_column_count))
            adapter = playersAdapter
        }

        viewModel = ViewModelProviders.of(this).get(TeamViewModel::class.java)
        viewModel.getPlayers().subscribe({ playerList ->
            players.apply {
                clear()
                addAll(playerList)
            }
            playersAdapter.notifyDataSetChanged()

            view.fab.setOnClickListener {
                context?.let {
                    val bundle = Bundle()
                    findNavController().navigate(R.id.playerEditFragment, bundle, NavigationUtils().getOptions())
                }
            }
        }, { throwable ->
            Timber.e(throwable)
        })

        return view
    }

    override fun onPlayerSelected(player: Player) {
        val extras = Bundle()
        extras.putLong(Constants.PLAYER_ID, player.id)
        findNavController().navigate(R.id.playerDetailsFragment, extras, NavigationUtils().getOptions())
    }
}
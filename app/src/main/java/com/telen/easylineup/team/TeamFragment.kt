package com.telen.easylineup.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_player_list.view.*

class TeamFragment: BaseFragment("TeamFragment"), OnPlayerClickListener {

    private lateinit var playersAdapter: TeamAdapter
    private lateinit var viewModel: TeamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clickable = arguments?.getBoolean(Constants.EXTRA_CLICKABLE) ?: true
        activity?.let {
            playersAdapter = TeamAdapter(it, when(clickable) {
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

        viewModel.observePlayers().observe(viewLifecycleOwner, Observer {
            playersAdapter.setPlayers(it)

            view.fab.setOnClickListener {
                context?.let {
                    FirebaseAnalyticsUtils.onClick(activity, "click_team_players_create")
                    findNavController().navigate(R.id.playerEditFragment, null, NavigationUtils().getOptions())
                }
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
        view?.teamPlayersRecyclerView?.adapter = null
    }

    override fun onPlayerSelected(player: Player) {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_selected")
        val extras = Bundle()
        extras.putLong(Constants.PLAYER_ID, player.id)
        findNavController().navigate(R.id.playerDetailsFragment, extras, NavigationUtils().getOptions())
    }
}
/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentPlayerListBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils

class PlayersFragment : BaseFragment("TeamFragment"), OnPlayerClickListener {
    private val playersAdapter = TeamAdapter(this)
    private val viewModel: TeamViewModel by viewModels()
    private var binding: FragmentPlayerListBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlayerListBinding.inflate(inflater, container, false).apply {
            this@PlayersFragment.binding = this
        }

        binding.fab.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(activity, "click_team_players_create")
            findNavController().navigate(
                R.id.playerEditFragment,
                null,
                NavigationUtils().getOptions()
            )
        }

        viewModel.observePlayers().observe(viewLifecycleOwner) {
            playersAdapter.submitList(it)
        }

        viewModel.observeDisplayType().observe(viewLifecycleOwner) {
            playersAdapter.displayType = it
            when (it) {
                TeamViewModel.DisplayType.LIST -> {
                    binding.displayMode.apply {
                        setChipIconResource(R.drawable.ic_baseline_grid_view_24)
                        setText(R.string.display_grid)
                    }
                    showList()
                }
                TeamViewModel.DisplayType.GRID -> {
                    binding.displayMode.apply {
                        setChipIconResource(R.drawable.ic_baseline_view_list_24)
                        setText(R.string.display_list)
                    }
                    showGrid()
                }
            }
        }

        binding.displayMode.setOnClickListener {
            viewModel.switchDisplayType()
            scrollTop()
        }

        binding.sortByName.setOnClickListener {
            viewModel.setSortType(TeamViewModel.SortType.ALPHA)
            scrollTop()
        }

        binding.sortByShirtNumber.setOnClickListener {
            viewModel.setSortType(TeamViewModel.SortType.NUMERIC)
            scrollTop()
        }

        showGrid()

        return binding.root
    }

    private fun showList() {
        binding?.teamPlayersRecyclerView?.apply {
            layoutManager = LinearLayoutManager(activity as AppCompatActivity)
            adapter = playersAdapter
        }
    }

    private fun showGrid() {
        binding?.teamPlayersRecyclerView?.apply {
            layoutManager = GridLayoutManager(
                activity as AppCompatActivity,
                resources.getInteger(R.integer.player_list_column_count)
            )
            adapter = playersAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
        binding = null
    }

    private fun scrollTop() {
        binding?.teamPlayersRecyclerView?.run {
            postDelayed({ scrollToPosition(0) }, 100)
        }
    }

    override fun onPlayerSelected(player: Player) {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_selected")
        val extras = Bundle()
        extras.putLong(Constants.PLAYER_ID, player.id)
        findNavController().navigate(
            R.id.playerDetailsFragment,
            extras,
            NavigationUtils().getOptions()
        )
    }
}

/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentPlayersDetailsContainerBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import timber.log.Timber

class PlayersDetailsContainerFragment : BaseFragment("PlayersDetailsContainerFragment") {
    private val teamViewModel by viewModels<TeamViewModel>()
    private val playerViewModel by viewModels<PlayerViewModel>()
    private var binding: FragmentPlayersDetailsContainerBinding? = null
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            pageSelected(position)
        }
    }
    private lateinit var playersAdapter: PlayersDetailsPagerAdapter
    private lateinit var pager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val playerId = playerViewModel.playerId.takeIf { it > 0 }
            ?: arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
        teamViewModel.setPlayerId(playerId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlayersDetailsContainerBinding.inflate(inflater, container, false)
        this.binding = binding

        playersAdapter = PlayersDetailsPagerAdapter(this)
        binding.viewPagerPlayersDetails.apply {
            pager = this
            this.adapter = playersAdapter
            registerOnPageChangeCallback(pageChangeCallback)
        }

        teamViewModel.observePlayers().observe(viewLifecycleOwner) { players ->
            playersAdapter.setPlayerIds(players)
            val playerIndex = playersAdapter.getPlayerIndex(teamViewModel.getPlayerId())
            pager.setCurrentItem(playerIndex, false)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.viewPagerPlayersDetails?.apply { adapter = null }
    }

    override fun onPause() {
        super.onPause()
        teamViewModel.clear()
        playerViewModel.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.player_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val selectedPlayerId = playersAdapter.getPlayerId(pager.currentItem)
        teamViewModel.setPlayerId(selectedPlayerId)

        return when (item.itemId) {
            R.id.action_edit -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_player_details_edit")
                val extras = Bundle().apply {
                    putLong(Constants.PLAYER_ID, selectedPlayerId)
                }
                findNavController().navigate(
                    R.id.playerEditFragment,
                    extras,
                    NavigationUtils().getOptions()
                )
                true
            }
            R.id.action_delete -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_player_details_delete")
                askUserConsentForDeletePlayerWithId(selectedPlayerId)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askUserConsentForDeletePlayerWithId(playerId: Long) {
        activity?.let { activity ->
            playerViewModel.playerId = playerId
            val task = playerViewModel.deletePlayer()
                .doOnComplete { findNavController().popBackStack(R.id.navigation_team, false) }
                .doOnError {
                    val message = "Something wrong happened: ${it.message}"
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                }
            DialogFactory.getWarningTaskDialog(
                context = activity,
                title = R.string.dialog_delete_player_title,
                message = R.string.dialog_delete_cannot_undo_message,
                task = task
            ).show()
        }
    }

    private fun pageSelected(position: Int) {
        if (position < playersAdapter.getPlayersSize()) {
            val playerId = playersAdapter.getPlayerId(position)
            playerViewModel.playerId = playerId
            teamViewModel.setPlayerId(playerId)
        } else {
            Timber.e(
                "player position is greater than the list size: " +
                        "position=%d but list size is %d", position, playersAdapter.getPlayersSize()
            )
        }
    }
}

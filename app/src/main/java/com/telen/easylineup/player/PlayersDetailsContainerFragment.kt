package com.telen.easylineup.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_players_details_container.view.*
import timber.log.Timber

class PlayersDetailsContainerFragment: Fragment() {

    private lateinit var mAdapter: PlayersDetailsPagerAdapter
    private val playersIds = mutableListOf<Long>()
    private lateinit var teamViewModel: TeamViewModel
    private lateinit var pager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = PlayersDetailsPagerAdapter(playersIds, childFragmentManager)
        teamViewModel = ViewModelProviders.of(this)[TeamViewModel::class.java]

        val playerID = savedInstanceState?.getLong(Constants.PLAYER_ID) ?: arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
        teamViewModel.setPlayerId(playerID)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(Constants.PLAYER_ID, teamViewModel.getPlayerId())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_players_details_container, container, false)
        pager = view.viewPagerPlayersDetails
        view.viewPagerPlayersDetails.adapter = mAdapter
        view.viewPagerPlayersDetails.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) { }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }

            override fun onPageSelected(position: Int) {
                if(position < playersIds.size) {
                    teamViewModel.setPlayerId(playersIds[position])
                }
                else {
                    Timber.e("player position is greater than the list size: position=$position but list size is ${playersIds.size} ")
                }
            }

        })
        return view
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        teamViewModel.getPlayers()
                .subscribe({ players ->
                    playersIds.clear()
                    playersIds.addAll(players.map { it.id })
                    mAdapter.notifyDataSetChanged()

                    val index = if(playersIds.indexOf(teamViewModel.getPlayerId()) >= 0) {
                        playersIds.indexOf(teamViewModel.getPlayerId())
                    } else  0 // not supposed to happen...

                    pager.setCurrentItem(index, false)
                }, {
                    Timber.e(it)
                })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.player_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val selectedPlayerId = playersIds[pager.currentItem]
        teamViewModel.setPlayerId(selectedPlayerId)

        return when (item.itemId) {
            R.id.action_edit -> {
                val extras = Bundle()
                extras.putLong(Constants.PLAYER_ID, selectedPlayerId)
                findNavController().navigate(R.id.playerEditFragment, extras, NavigationUtils().getOptions())
                true
            }
            R.id.action_delete -> {
                askUserConsentForDeletePlayerWithId(selectedPlayerId)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askUserConsentForDeletePlayerWithId(playerID: Long) {
        activity?.let {
            val playerViewModel = ViewModelProviders.of(this)[PlayerViewModel::class.java]
            playerViewModel.playerID = playerID
            DialogFactory.getWarningDialog(it,
                    it.getString(R.string.dialog_delete_player_title),
                    it.getString(R.string.dialog_delete_cannot_undo_message),
                    playerViewModel.deletePlayer()
                            .doOnComplete {
                                FragmentActivity@it.runOnUiThread {
                                    findNavController().popBackStack(R.id.navigation_team, false)
                                }
                            }.doOnError {
                                Toast.makeText(activity, "Something wrong happened: ${it.message}", Toast.LENGTH_LONG).show()
                            })
                    .show()
        }
    }
}
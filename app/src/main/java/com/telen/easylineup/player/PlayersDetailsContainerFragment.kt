package com.telen.easylineup.player

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import io.reactivex.Completable
import kotlinx.android.synthetic.main.fragment_players_details_container.view.*
import timber.log.Timber

class PlayersDetailsContainerFragment: BaseFragment("PlayersDetailsContainerFragment"), ViewPager.OnPageChangeListener {

    private lateinit var mAdapter: PlayersDetailsPagerAdapter
    private lateinit var teamViewModel: TeamViewModel
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var pager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = PlayersDetailsPagerAdapter(childFragmentManager)
        teamViewModel = ViewModelProviders.of(this)[TeamViewModel::class.java]
        playerViewModel = ViewModelProviders.of(this)[PlayerViewModel::class.java]
        val playerID = savedInstanceState?.getLong(Constants.PLAYER_ID) ?: arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
        teamViewModel.setPlayerId(playerID)

        val disposable = playerViewModel.registerEvent().subscribe({
            when(it) {
                DeletePlayerSuccess -> {
                    findNavController().popBackStack(R.id.navigation_team, false)
                }
                is DeletePlayerFailure -> {
                    Toast.makeText(activity, "Something wrong happened: ${it.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }, {
            Timber.e(it)
        })
        disposables.add(disposable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(Constants.PLAYER_ID, teamViewModel.getPlayerId())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_players_details_container, container, false)

        view.viewPagerPlayersDetails.apply {
            pager = this
            adapter = mAdapter
            addOnPageChangeListener(this@PlayersDetailsContainerFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        view?.viewPagerPlayersDetails?.apply {
//            adapter = null
//        }
    }

    override fun onResume() {
        super.onResume()

        teamViewModel.observePlayers().observe(viewLifecycleOwner, Observer { players ->
            mAdapter.setPlayerIDs(players)
            pager.setCurrentItem(mAdapter.getPlayerIndex(teamViewModel.getPlayerId()), false)
        })
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

        val selectedPlayerId = mAdapter.getPlayerID(pager.currentItem)
        teamViewModel.setPlayerId(selectedPlayerId)

        return when (item.itemId) {
            R.id.action_edit -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_player_details_edit")
                val extras = Bundle()
                extras.putLong(Constants.PLAYER_ID, selectedPlayerId)
                findNavController().navigate(R.id.playerEditFragment, extras, NavigationUtils().getOptions())
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

    private fun askUserConsentForDeletePlayerWithId(playerID: Long) {
        activity?.let {
            playerViewModel.playerID = playerID
            DialogFactory.getWarningTaskDialog(context = it,
                            title = R.string.dialog_delete_player_title,
                            message = R.string.dialog_delete_cannot_undo_message,
                            task = Completable.create { emitter ->
                                playerViewModel.deletePlayer()
                                emitter.onComplete()
                            })
                    .show()
        }
    }

    override fun onPageScrollStateChanged(state: Int) { }
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }

    override fun onPageSelected(position: Int) {
        if(position < mAdapter.getPlayersSize()) {
            teamViewModel.setPlayerId(mAdapter.getPlayerID(position))
        }
        else {
            Timber.e("player position is greater than the list size: position=$position but list size is ${mAdapter.getPlayersSize()} ")
        }
    }
}
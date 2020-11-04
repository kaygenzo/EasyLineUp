package com.telen.easylineup.team.details

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.lineup.list.LineupViewModel
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import io.reactivex.Completable
import kotlinx.android.synthetic.main.fragment_team_details.*
import timber.log.Timber

const val REQUEST_EDIT_TEAM = 0

class TeamDetailsFragment: BaseFragment("TeamDetailsFragment") {

    private lateinit var teamViewModel: TeamViewModel
    private lateinit var lineupViewModel: LineupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        teamViewModel = ViewModelProviders.of(this)[TeamViewModel::class.java]
        lineupViewModel = ViewModelProviders.of(this)[LineupViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_details, container, false)
        load()
        return view
    }

    private fun load() {
        teamViewModel.observeTeam().observe(viewLifecycleOwner, Observer {
            view?.run {
                teamTypeRootView?.run {
                    setTeamName(it.name.trim())
                    setTeamType(it.type)
                    setTeamImage(it.image)
                    setDragEnabled(true)
                    setDragState(BottomSheetBehavior.STATE_HALF_EXPANDED)
                }
            }
        })

        teamViewModel.observePlayers().observe(viewLifecycleOwner, Observer {
            val description = resources.getQuantityString(R.plurals.team_details_team_size, it.size, it.size)
            view?.run {
                teamTypeRootView?.run {
                    setPlayersSize(R.drawable.ic_team, description)
                }
            }
        })

        lineupViewModel.observeCategorizedLineups().observe(viewLifecycleOwner, Observer {
            val tournamentsSize = it.size
            val lineupsSize = it.map { pair -> pair.second.size }.sum()
            val tournamentsQuantity = resources.getQuantityString(R.plurals.tournaments_quantity, tournamentsSize, tournamentsSize)
            val lineupsQuantity = resources.getQuantityString(R.plurals.lineups_quantity, lineupsSize, lineupsSize)
            val headerText = getString(R.string.tournaments_summary_header, tournamentsQuantity, lineupsQuantity)
            view?.run {
                teamTypeRootView?.run {
                    setLineupsSize(R.drawable.ic_list_black_24dp, headerText)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        teamViewModel.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        arguments?.getInt(Constants.EXTRA_TEAM_COUNT)?.let {
            if(it < 2)
                inflater.inflate(R.menu.team_edit_menu, menu)
            else {
                inflater.inflate(R.menu.team_edit_or_delete_menu, menu)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                teamViewModel.team?.let {
                    FirebaseAnalyticsUtils.startTutorial(activity, false)
                    val intent = Intent(activity, TeamCreationActivity::class.java)
                    intent.putExtra(Constants.EXTRA_CAN_EXIT, true)
                    intent.putExtra(Constants.EXTRA_TEAM, it)
                    startActivityForResult(intent, REQUEST_EDIT_TEAM)
                }
                true
            }
            R.id.action_delete -> {
                activity?.let {
                    teamViewModel.team?.let { team ->
                        DialogFactory.getWarningTaskDialog(context = it,
                                title = R.string.dialog_delete_team_title,
                                titleArgs = arrayOf(team.name),
                                message = R.string.dialog_delete_cannot_undo_message,
                                task = Completable.create { emitter ->
                                    val disposable = teamViewModel.deleteTeam(team)
                                            .subscribe({
                                                findNavController().popBackStack()
                                            }, {
                                                Timber.e(it)
                                            })
                                    disposables.add(disposable)
                                    emitter.onComplete()
                                })
                                .show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_EDIT_TEAM -> {
                load()
            }
        }
    }
}
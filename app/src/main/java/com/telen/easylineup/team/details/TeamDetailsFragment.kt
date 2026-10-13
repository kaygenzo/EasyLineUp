/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.tournaments.list.LineupViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.rx3.rxCompletable

class TeamDetailsFragment : BaseFragment("TeamDetailsFragment") {
    private val teamViewModel: TeamViewModel by viewModels()
    private val lineupViewModel: LineupViewModel by viewModels()
    private val editTeam =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            teamViewModel.loadTeam()
        }

    private var teamName by mutableStateOf("")
    private var teamType by mutableStateOf(TeamType.UNKNOWN)
    private var teamImageUri by mutableStateOf<Uri?>(null)
    private var rosterSize by mutableStateOf(0)
    private var lineupsBuilt by mutableStateOf(0)
    private var tournamentsCount by mutableStateOf(0)
    private var maleCount by mutableStateOf(0)
    private var femaleCount by mutableStateOf(0)
    private var unsetCount by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val canDelete = (arguments?.getInt(Constants.EXTRA_TEAM_COUNT) ?: 0) >= 2

        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    TeamDetailsScreen(
                        state = TeamDetailsState(
                            teamName = teamName,
                            teamType = teamType,
                            teamImageUri = teamImageUri,
                            rosterSize = rosterSize,
                            lineupsBuilt = lineupsBuilt,
                            tournamentsCount = tournamentsCount,
                            maleCount = maleCount,
                            femaleCount = femaleCount,
                            unsetCount = unsetCount,
                            canDelete = canDelete
                        ),
                        onDeleteClick = ::showDeleteConfirmation
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamViewModel.observeCurrentTeamName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        teamViewModel.observeCurrentTeamType()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamType = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        teamViewModel.observeCurrentTeamImage()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamImageUri = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        teamViewModel.observePlayers()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { players ->
                rosterSize = players.size
                maleCount = players.count { it.sex == Sex.MALE.id }
                femaleCount = players.count { it.sex == Sex.FEMALE.id }
                unsetCount = players.count { it.sex == Sex.UNKNOWN.id }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        lineupViewModel.observeCategorizedLineups()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                tournamentsCount = it.size
                lineupsBuilt = it.sumOf { item -> item.lineups.count() }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        teamViewModel.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.team_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_team_details_edit")
                teamViewModel.team?.let {
                    FirebaseAnalyticsUtils.startTutorial(activity, false)
                    val intent = Intent(activity, TeamCreationActivity::class.java)
                    intent.putExtra(Constants.EXTRA_CAN_EXIT, true)
                    intent.putExtra(Constants.EXTRA_TEAM, it)
                    editTeam.launch(intent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmation() {
        activity?.let {
            teamViewModel.team?.let { team ->
                DialogFactory.getWarningTaskDialog(
                    context = it,
                    title = R.string.dialog_delete_team_title,
                    titleArgs = arrayOf(team.name),
                    message = R.string.dialog_delete_cannot_undo_message,
                    task = rxCompletable {
                        FirebaseAnalyticsUtils.onClick(activity, "click_team_details_delete")
                        teamViewModel.deleteTeam(team).getOrThrow()
                    }.doOnComplete { findNavController().popBackStack() }
                ).show()
            }
        }
    }
}

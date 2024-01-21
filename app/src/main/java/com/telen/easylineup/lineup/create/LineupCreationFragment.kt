/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.getkeepsafe.taptargetview.TapTargetView
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentLineupCreationBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.tournaments.CreateTournamentDialog
import com.telen.easylineup.tournaments.list.LineupViewModel
import com.telen.easylineup.tournaments.list.SaveSuccess
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.LineupCreationFormView
import com.telen.easylineup.views.OnActionButtonListener
import timber.log.Timber

class LineupCreationFragment : BaseFragment("LineupCreationFragment"), OnActionButtonListener {
    private val lineupViewModel by viewModels<LineupViewModel>()
    private var binding: FragmentLineupCreationBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch(lineupViewModel.observeErrors(), {
            when (it) {
                DomainErrors.Lineups.INVALID_TOURNAMENT_NAME -> {
                    binding?.lineupCreationForm?.setTournamentNameError(getString(
                        R.string.lineup_creation_error_tournament_empty
                    ))
                    FirebaseAnalyticsUtils.emptyTournamentName(activity)
                }

                DomainErrors.Lineups.INVALID_LINEUP_NAME -> {
                    binding?.lineupCreationForm?.setLineupNameError(getString(
                        R.string.lineup_creation_error_name_empty
                    ))
                    FirebaseAnalyticsUtils.emptyLineupName(activity)
                }

                else -> {}
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentLineupCreationBinding.inflate(inflater, container, false).apply {
            this@LineupCreationFragment.binding = this
            lineupViewModel.getTournaments().observe(viewLifecycleOwner) {
                lineupCreationForm.setList(it)
            }

            launch(lineupViewModel.getTeamType(), {
                lineupCreationForm.setTeamType(TeamType.getTypeById(it))
            })

            lineupCreationForm.setFragmentManager(childFragmentManager)
            lineupCreationForm.setOnActionClickListener(this@LineupCreationFragment)

            launch(lineupViewModel.getCompleteRoster(), {
                updateRosterSize(lineupCreationForm.binding.playerCount, it)
            })

            lineupViewModel.registerSaveResults().observe(viewLifecycleOwner) {
                when (it) {
                    is SaveSuccess -> {
                        Timber.d("successfully inserted new lineup, new id: ${it.lineup.id}")
                        val extras = LineupFragment.getArguments(it.lineup.id)
                        findNavController().navigate(
                            R.id.lineupFragmentEditable,
                            extras,
                            NavigationUtils().getOptionsWithPopDestination(
                                R.id.navigation_lineups,
                                false
                            )
                        )
                    }
                }
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun showRosterDialog(formView: LineupCreationFormView) {
        launch(lineupViewModel.getChosenRoster(), { response ->
            activity?.let { activity ->
                val names: MutableList<CharSequence> = mutableListOf()
                names.addAll(response.players.map { it.player.name })
                val checked: MutableList<Boolean> = mutableListOf()
                checked.addAll(response.players.map { it.status })
                DialogFactory.getMultiChoiceDialog(
                    context = activity,
                    title = R.string.roster_list_player_dialog_title,
                    items = names.toTypedArray(),
                    checkedItems = checked.toBooleanArray(),
                    listener = { _, which, isChecked ->
                        lineupViewModel.rosterPlayerStatusChanged(which, isChecked)
                        updateRosterSize(formView.binding.playerCount, response)
                    }
                ).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity ->
            launch(lineupViewModel.showNewRosterFeature(), { show ->
                if (show) {
                    binding?.lineupCreationForm?.let { form ->
                        FeatureViewFactory.apply(form.binding.rosterExpandableEdit,
                            activity as AppCompatActivity,
                            getString(R.string.feature_roster_title),
                            getString(R.string.feature_roster_description),
                            object : TapTargetView.Listener() {
                                override fun onTargetClick(view: TapTargetView?) {
                                    showRosterDialog(form)
                                    view?.dismiss(true)
                                }

                                override fun onOuterCircleClick(view: TapTargetView?) {
                                    view?.dismiss(false)
                                }
                            })
                    }
                }
            })
        }
    }

    private fun updateRosterSize(view: TextView, response: TeamRosterSummary) {
        when (response.status) {
            Constants.STATUS_ALL -> view.text = getString(R.string.roster_size_status_all)

            else -> {
                val size = response.players.filter { it.status }.size
                view.text =
                        resources.getQuantityString(R.plurals.roster_size_status_selection, size, size)
            }
        }
    }

    override fun onRosterChangeClicked() {
        binding?.lineupCreationForm?.let { showRosterDialog(it) }
    }

    override fun onCreateTournamentClicked() {
        CreateTournamentDialog {
            launch(lineupViewModel.saveTournament(it), {
                Timber.d("Tournament saved")
                binding?.lineupCreationForm?.selectTournament(it)
                lineupViewModel.onTournamentSelected(it)
            }, { Timber.e(it) })
        }.show(childFragmentManager, "createTournamentFragment")
    }

    override fun onTournamentSelected(tournament: Tournament) {
        lineupViewModel.onTournamentSelected(tournament)
    }

    override fun onLineupStartTimeChanged(time: Long) {
        lineupViewModel.onLineupStartTimeChanged(time)
    }

    override fun onLineupNameChanged(name: String) {
        lineupViewModel.onLineupNameChanged(name)
    }

    override fun onStrategyChanged(strategy: TeamStrategy) {
        lineupViewModel.onStrategyChanged(strategy)
    }

    override fun onExtraHittersChanged(count: Int) {
        lineupViewModel.onExtraHittersChanged(count)
    }

    override fun onSaveClicked() {
        lineupViewModel.saveLineup()
    }

    override fun onCancelClicked() {
        findNavController().popBackStack()
    }
}

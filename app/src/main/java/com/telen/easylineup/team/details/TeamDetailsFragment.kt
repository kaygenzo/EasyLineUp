package com.telen.easylineup.team.details

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentTeamDetailsBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.tournaments.list.LineupViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import io.reactivex.rxjava3.core.Completable

class TeamDetailsFragment : BaseFragment("TeamDetailsFragment") {

    private val teamViewModel: TeamViewModel by viewModels()
    private val lineupViewModel: LineupViewModel by viewModels()

    private var binding: FragmentTeamDetailsBinding? = null

    private val editTeam =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            teamViewModel.loadTeam()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTeamDetailsBinding.inflate(inflater, container, false)
        this.binding = binding

        teamViewModel.observeCurrentTeamName().observe(viewLifecycleOwner) {
            binding.teamTypeRootView.setTeamName(it)
        }

        teamViewModel.observeCurrentTeamType().observe(viewLifecycleOwner) {
            binding.teamTypeRootView.setTeamType(it.id)
        }

        teamViewModel.observeCurrentTeamImage().observe(viewLifecycleOwner) {
            binding.teamTypeRootView.setTeamImage(it)
        }

        binding.teamTypeRootView.apply {
            setDragEnabled(true)
            setDragState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }

        teamViewModel.observePlayers().observe(viewLifecycleOwner) {
            val description =
                resources.getQuantityString(R.plurals.team_details_team_size, it.size, it.size)
            binding.teamTypeRootView.setPlayersSize(R.drawable.ic_team, description)

            val womenCount = it.filter { it.sex == Sex.FEMALE.id }.size.let {
                resources.getQuantityString(R.plurals.women_count, it, it)
            }
            val menCount = it.filter { it.sex == Sex.MALE.id }.size.let {
                resources.getQuantityString(R.plurals.men_count, it, it)
            }
            val sexUnsetCount = it.filter { it.sex == Sex.UNKNOWN.id }.size.let {
                resources.getQuantityString(R.plurals.unset_sex_count, it, it)
            }
            val sexRepartitionCountMessage = "$womenCount, $menCount, $sexUnsetCount"
            binding.teamTypeRootView.setSexStats(
                R.drawable.ic_baseline_diversity_2_24,
                sexRepartitionCountMessage
            )
        }

        lineupViewModel.observeCategorizedLineups().observe(viewLifecycleOwner) {
            val tournamentsSize = it.size
            val lineupsSize = it.sumOf { pair -> pair.second.size }
            val tournamentsQuantity = resources.getQuantityString(
                R.plurals.tournaments_quantity,
                tournamentsSize,
                tournamentsSize
            )
            val lineupsQuantity =
                resources.getQuantityString(R.plurals.lineups_quantity, lineupsSize, lineupsSize)
            val headerText =
                getString(R.string.tournaments_summary_header, tournamentsQuantity, lineupsQuantity)
            binding.teamTypeRootView.setLineupsSize(R.drawable.ic_list_black_24dp, headerText)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        teamViewModel.clear()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        arguments?.getInt(Constants.EXTRA_TEAM_COUNT)?.let {
            if (it < 2) {
                inflater.inflate(R.menu.team_edit_menu, menu)
            } else {
                inflater.inflate(R.menu.team_edit_or_delete_menu, menu)
            }
        }
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
            R.id.action_delete -> {
                activity?.let {
                    teamViewModel.team?.let { team ->
                        DialogFactory.getWarningTaskDialog(
                            context = it,
                            title = R.string.dialog_delete_team_title,
                            titleArgs = arrayOf(team.name),
                            message = R.string.dialog_delete_cannot_undo_message,
                            task = Completable.defer {
                                FirebaseAnalyticsUtils.onClick(
                                    activity,
                                    "click_team_details_delete"
                                )
                                teamViewModel.deleteTeam(team)
                            }.doOnComplete { findNavController().popBackStack() }
                        ).show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
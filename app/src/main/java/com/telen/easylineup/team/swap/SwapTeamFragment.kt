package com.telen.easylineup.team.swap

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telen.easylineup.R
import com.telen.easylineup.databinding.TeamsListViewBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.utils.FirebaseAnalyticsUtils

interface SwapTeamActions {
    fun onTeamClick(team: Team)
    fun onCreateTeamClick()
}

data class ActivityNullException(override val message: String) : Exception(message)

class SwapTeamFragment : DialogFragment() {

    private var mAdapter: SwapTeamsListAdapter? = null
    private var mSwapTeamActions: SwapTeamActions? = null
    private var binding: TeamsListViewBinding? = null

    fun setSwapTeamActionsListener(swapTeamActions: SwapTeamActions) {
        mSwapTeamActions = swapTeamActions
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity?.let { activity ->
            val binding = TeamsListViewBinding.inflate(activity.layoutInflater, null, false).apply {
                this@SwapTeamFragment.binding = this
            }

            val teams =
                arguments?.getSerializable(Constants.EXTRA_TEAMS) as? List<Team> ?: mutableListOf()

            binding.noTeamsView.run {
                setImageHint(R.drawable.ic_oobe_conv_list)
                if (teams.isEmpty()) {
                    setTextHint(R.string.team_list_empty_text)
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }

            mAdapter =
                SwapTeamsListAdapter(teams, object : SwapTeamsListAdapter.SwapTeamsAdapterListener {
                    override fun onTeamClicked(team: Team) {
                        FirebaseAnalyticsUtils.onClick(activity, "click_team_swap_selection")
                        mSwapTeamActions?.onTeamClick(team)
                        dismiss()
                    }
                })

            binding.list.apply {
                layoutManager = object : LinearLayoutManager(activity) {
                    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
                        return RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
                setHasFixedSize(true)
                adapter = mAdapter
            }

            val dialogBuilder = MaterialAlertDialogBuilder(activity)
                .setCancelable(true)
                .setView(binding.root)
                .setTitle(R.string.team_list_dialog_title)

            dialogBuilder.setPositiveButton(R.string.team_list_dialog_create) { dialog, which ->
                FirebaseAnalyticsUtils.onClick(activity, "click_team_swap_create")
                mSwapTeamActions?.onCreateTeamClick()
                dismiss()
            }

            return dialogBuilder.setNegativeButton(android.R.string.cancel, null)
                .create()

        } ?: throw ActivityNullException("Activity is null, it's not allowed at this step")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        binding = null
    }
}
package com.telen.easylineup.team.swap

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.views.ListEmptyView
import kotlinx.android.synthetic.main.teams_list_view.view.*

interface HostInterface {
    fun onTeamClick(team: Team)
    fun onCreateTeamClick()
}

data class ActivityNullException(override val message: String): Exception(message)

class SwapTeamFragment: DialogFragment() {

    private var mAdapter: SwapTeamsListAdapter? = null
    private var mHostInterface: HostInterface? = null

    fun setHostInterface(hostInterface: HostInterface) {
        mHostInterface = hostInterface
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity?.let { activity ->
            val inflater = activity.layoutInflater
            val view = inflater.inflate(R.layout.teams_list_view, null)
            val mEmptyListMessageView = view.findViewById(R.id.no_teams_view) as ListEmptyView
            mEmptyListMessageView.setImageHint(R.drawable.ic_oobe_conv_list)

            val teams = arguments?.getSerializable(Constants.EXTRA_TEAM) as? List<Team> ?: mutableListOf()

            if (teams.isEmpty()) {
                mEmptyListMessageView.setTextHint(R.string.team_list_empty_text)
                mEmptyListMessageView.visibility = View.VISIBLE
            } else {
                mEmptyListMessageView.visibility = View.GONE
            }

            val manager = object : LinearLayoutManager(activity) {
                override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
                    return RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            }

            mAdapter = SwapTeamsListAdapter(teams, object : SwapTeamsListAdapter.HostInterface {
                override fun onTeamClicked(team: Team) {
                    FirebaseAnalyticsUtils.onClick(activity, "click_team_swap_selection")
                    mHostInterface?.onTeamClick(team)
                    dismiss()
                }
            })
            view.list.apply {
                layoutManager = manager
                setHasFixedSize(true)
                adapter = mAdapter
            }

            val dialogBuilder = MaterialAlertDialogBuilder(activity)
                    .setCancelable(true)
                    .setView(view)
                    .setTitle(R.string.team_list_dialog_title)

            dialogBuilder.setPositiveButton(R.string.team_list_dialog_create) { dialog, which ->
                FirebaseAnalyticsUtils.onClick(activity, "click_team_swap_create")
                mHostInterface?.onCreateTeamClick()
                dismiss()
            }

            return dialogBuilder.setNegativeButton(android.R.string.cancel, null)
                    .create()

        } ?: throw ActivityNullException("Activity is null, it's not allowed at this step")
    }
}
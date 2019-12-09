package com.telen.easylineup.team.swap

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.views.ListEmptyView
import kotlinx.android.synthetic.main.teams_list_view.view.*

interface HostInterface {
    fun onTeamClick(team: Team)
    fun onCreateTeamClick()
}

class SwapTeamFragment(private val teams: List<Team>, private val hostInterface: HostInterface): DialogFragment() {

    private var mAdapter: SwapTeamsListAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            val inflater = activity.layoutInflater
            val view = inflater.inflate(R.layout.teams_list_view, null)
            val mEmptyListMessageView = view.findViewById(R.id.no_teams_view) as ListEmptyView
            mEmptyListMessageView.setImageHint(R.drawable.ic_oobe_conv_list)

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
                    hostInterface.onTeamClick(team)
                    dismiss()
                }
            })
            view.list.apply {
                layoutManager = manager
                setHasFixedSize(true)
                adapter = mAdapter
            }

            val dialogBuilder = AlertDialog.Builder(activity)
                    .setView(view)
                    .setTitle(R.string.team_list_dialog_title)

            dialogBuilder.setPositiveButton(R.string.team_list_dialog_create) { dialog, which ->
                hostInterface.onCreateTeamClick()
                dismiss()
            }

            return dialogBuilder.setNegativeButton(android.R.string.cancel, null)
                    .create()

        } ?: throw Exception("Activity is null, it's not allowed at this step")
    }

}
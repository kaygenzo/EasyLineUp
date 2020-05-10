package com.telen.easylineup.team.swap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Team


class SwapTeamsListAdapter(private val teams: List<Team>, private val hostInterface: HostInterface): RecyclerView.Adapter<SwapTeamsListAdapter.TeamsListViewHolder>() {

    interface HostInterface {
        fun onTeamClicked(team: Team)
    }

    override fun getItemCount(): Int {
        return teams.size
    }

    override fun onBindViewHolder(holder: TeamsListViewHolder, position: Int) {
        val team = teams[position]
        holder.view.setTeamName(team.name)
        holder.view.setImage(team.image, team.name)
        holder.view.setOnClickListener {
            hostInterface.onTeamClicked(team)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamsListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.teams_list_item_view, null) as TeamItemView
        return TeamsListViewHolder(itemView)
    }

    inner class TeamsListViewHolder(val view: TeamItemView): RecyclerView.ViewHolder(view)

}
/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.swap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.databinding.TeamsListItemViewBinding
import com.telen.easylineup.domain.model.Team

class SwapTeamsListAdapter(
    private val teams: List<Team>,
    private val listener: SwapTeamsAdapterListener
) : RecyclerView.Adapter<SwapTeamsListAdapter.TeamsListViewHolder>() {
    override fun getItemCount(): Int {
        return teams.size
    }

    override fun onBindViewHolder(holder: TeamsListViewHolder, position: Int) {
        val team = teams[position]
        holder.view.setTeamName(team.name)
        holder.view.setImage(team.image, team.name)
        holder.view.setOnClickListener { listener.onTeamClicked(team) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamsListViewHolder {
        val itemView =
            TeamsListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamsListViewHolder(itemView.root)
    }

    interface SwapTeamsAdapterListener {
        fun onTeamClicked(team: Team)
    }

    /**
     * @property view
     */
    inner class TeamsListViewHolder(val view: TeamItemView) : RecyclerView.ViewHolder(view)
}

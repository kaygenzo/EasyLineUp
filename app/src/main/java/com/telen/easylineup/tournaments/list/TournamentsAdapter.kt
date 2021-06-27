package com.telen.easylineup.tournaments.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.views.OnActionsClickListener
import com.telen.easylineup.views.TournamentItemView

sealed class TimeLineItem
data class TournamentItem(val tournament: Tournament, val start: Long?, val end: Long?, val lineups: List<Lineup>) : TimeLineItem()

const val TYPE_ITEM_TOURNAMENT = 1

interface OnItemClickedListener {
    fun onHeaderClicked()
    fun onLineupClicked(lineup: Lineup)
    fun onDeleteTournamentClicked(tournament: Tournament)
    fun onStatisticsTournamentClicked(teamType:TeamType, tournament: Tournament)
}

class TournamentsAdapter(private val itemClickListener: OnItemClickedListener, private var teamType: Int = TeamType.BASEBALL.id): RecyclerView.Adapter<TournamentsAdapter.TournamentsViewHolder>() {

    private val items: MutableList<TimeLineItem> = mutableListOf()

    override fun getItemViewType(position: Int): Int {
        return TYPE_ITEM_TOURNAMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentsViewHolder {
        val view = TournamentItemView(parent.context, itemClickListener)
        return TournamentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TournamentsViewHolder, position: Int) {
        val view = holder.view as TournamentItemView
        val item = items[position] as TournamentItem

        view.setTournamentName(item.tournament.name)
        view.setTeamType(this.teamType)
        view.setLineups(item.lineups)

        if(item.start != null && item.end != null)
            view.setTournamentDate(item.start, item.end)

        view.setOnActionsClickListener(object: OnActionsClickListener {
            override fun onDeleteClicked() {
                itemClickListener.onDeleteTournamentClicked(item.tournament)
            }

            override fun onStatsClicked() {
                itemClickListener.onStatisticsTournamentClicked(TeamType.getTypeById(teamType), item.tournament)
            }
        })
    }

    class TournamentsViewHolder(val view: View): RecyclerView.ViewHolder(view)

    fun setList(tournaments: List<Pair<Tournament, List<Lineup>>>) {
        items.clear()
        tournaments.forEach {
            val tournamentStart = it.second.map { it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis }.minOrNull()
            val tournamentEnd = it.second.map {it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis }.maxOrNull()
            items.add(TournamentItem(it.first, tournamentStart, tournamentEnd, it.second))
        }
        notifyDataSetChanged()
    }

    fun setTeamType(teamType: Int) {
        this.teamType = teamType
        notifyDataSetChanged()
    }
}
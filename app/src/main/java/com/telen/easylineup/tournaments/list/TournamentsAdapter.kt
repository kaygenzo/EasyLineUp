package com.telen.easylineup.tournaments.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.views.OnActionsClickListener
import com.telen.easylineup.views.TournamentItemView

const val TYPE_ITEM_TOURNAMENT = 1

class TournamentsAdapter(
    private val tournamentItemListener: OnTournamentItemListener,
    private val items: MutableList<TournamentItem> = mutableListOf(),
    private val mapsMap: MutableMap<Long, String> = mutableMapOf(),
    private var teamType: Int = TeamType.BASEBALL.id
) : RecyclerView.Adapter<TournamentsAdapter.TournamentsViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return TYPE_ITEM_TOURNAMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentsViewHolder {
        val view = TournamentItemView(parent.context, tournamentItemListener)
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

        view.setTournamentMapVisible(item.tournament.address != null)
        view.setTournamentMap(mapsMap[item.tournament.id])

        val start = item.getStart()
        val end = item.getEnd()
        if (start != null && end != null) {
            view.setTournamentDate(start, end)
        }

        view.setOnActionsClickListener(object : OnActionsClickListener {
            override fun onDeleteClicked() {
                tournamentItemListener.onDeleteTournamentClicked(item.tournament)
            }

            override fun onStatsClicked() {
                tournamentItemListener.onStatisticsTournamentClicked(
                    TeamType.getTypeById(teamType),
                    item.tournament
                )
            }
        })
    }

    class TournamentsViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    fun setList(list: List<TournamentItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setTeamType(teamType: Int) {
        this.teamType = teamType
        notifyDataSetChanged()
    }

    fun setMapToTournament(tournament: Tournament, url: String) {
        mapsMap[tournament.id] = url
        items.indexOfFirst { it.tournament.id == tournament.id }.takeIf { it >= 0 }?.let {
            notifyItemChanged(it)
        }
    }
}
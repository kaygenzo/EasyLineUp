package com.telen.easylineup.lineup.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.Tournament
import com.telen.easylineup.views.*
import pl.hypeapp.materialtimelineview.MaterialTimelineView

sealed class TimeLineItem
data class HeaderTimeLineItem(val position: Int, val tournamentsSize: Int, val lineupsSize: Int) : TimeLineItem()
data class EmptyTimeLineItem(val position: Int) : TimeLineItem()
data class TimeLineLineItem(val tournament: Tournament) : TimeLineItem()
data class TimelineTopView(val tournament: Tournament, val lineups: List<Lineup>): TimeLineItem()
data class TimelineMiddleView(val tournament: Tournament, val lineups: List<Lineup>): TimeLineItem()
data class TimelineBottomView(val tournament: Tournament, val lineups: List<Lineup>): TimeLineItem()

const val TYPE_ITEM_HEADER = 0
const val TYPE_ITEM_VIEW = 1
const val TYPE_ITEM_LINE = 2
const val TYPE_ITEM_EMPTY = 3

interface OnItemClickedListener {
    fun onHeaderClicked()
    fun onLineupClicked(lineup: Lineup)
    fun onTournamentLongClicked(tournament: Tournament)
}

class TournamentsAdapter(private val itemClickListener: OnItemClickedListener): RecyclerView.Adapter<TournamentsAdapter.TournamentsViewHolder>() {

    private val items: MutableList<TimeLineItem> = mutableListOf()

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is TimeLineLineItem -> TYPE_ITEM_LINE
            is HeaderTimeLineItem -> TYPE_ITEM_HEADER
            is TimelineTopView -> TYPE_ITEM_VIEW
            is TimelineMiddleView -> TYPE_ITEM_VIEW
            is TimelineBottomView -> TYPE_ITEM_VIEW
            is EmptyTimeLineItem -> TYPE_ITEM_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentsViewHolder {
        return when(viewType) {
            TYPE_ITEM_LINE -> {
                val view = TimeLineMiddleView(parent.context)
                TournamentsViewHolder(view)
            }
            TYPE_ITEM_HEADER -> {
                val view = TimeLineHeaderView(parent.context)
                TournamentsViewHolder(view)
            }
            TYPE_ITEM_EMPTY -> {
                val view = TimeLineEmptyView(parent.context)
                TournamentsViewHolder(view)
            }
            else -> {
                val view = TournamentItemView(parent.context, itemClickListener)
                TournamentsViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TournamentsViewHolder, position: Int) {
        when(items[position]) {
            is HeaderTimeLineItem -> {
                val view = holder.view as TimeLineHeaderView
                val item = items[position] as HeaderTimeLineItem
                view.setTimeLinePosition(item.position)

                val tournamentsQuantity = view.context.resources.getQuantityString(R.plurals.tournaments_quantity, item.tournamentsSize, item.tournamentsSize)
                val lineupsQuantity = view.context.resources.getQuantityString(R.plurals.lineups_quantity, item.lineupsSize, item.lineupsSize)
                val headerText = view.context.getString(R.string.tournaments_summary_header, tournamentsQuantity, lineupsQuantity)
                view.setHeaderText(headerText)
            }
            is TimeLineLineItem -> {
                val view = holder.view as TimeLineMiddleView
                val item = items[position] as TimeLineLineItem
                view.setTournamentName(item.tournament.name)
                view.setTournamentDate(item.tournament.createdAt)
                view.setOnLongClickListener {
                    itemClickListener.onTournamentLongClicked(item.tournament)
                    true
                }
            }
            is TimelineTopView -> {
                val view = holder.view as TournamentItemView
                val item = items[position] as TimelineTopView
                view.setTimeLinePosition(MaterialTimelineView.POSITION_FIRST)
                view.setLineups(item.lineups)
            }
            is TimelineMiddleView -> {
                val view = holder.view as TournamentItemView
                val item = items[position] as TimelineMiddleView
                view.setTimeLinePosition(MaterialTimelineView.POSITION_MIDDLE)
                view.setLineups(item.lineups)
            }
            is TimelineBottomView -> {
                val view = holder.view as TournamentItemView
                val item = items[position] as TimelineBottomView
                view.setTimeLinePosition(MaterialTimelineView.POSITION_LAST)
                view.setLineups(item.lineups)
            }
            is EmptyTimeLineItem -> {
                val view = holder.view as TimeLineEmptyView
                val item = items[position] as EmptyTimeLineItem
                view.setTimeLinePosition(item.position)
            }
        }
    }

    class TournamentsViewHolder(val view: View): RecyclerView.ViewHolder(view)

    fun setList(tournaments: List<Pair<Tournament, List<Lineup>>>) {
        items.clear()
        var index = 0
        val tournamentsSize = tournaments.size
        val lineupsSize = tournaments.map { pair -> pair.second.size }.sum()
        if(tournaments.isEmpty()) {
            items.add(HeaderTimeLineItem(-1, tournamentsSize, lineupsSize))
        }
        else
            items.add(HeaderTimeLineItem(MaterialTimelineView.POSITION_FIRST, tournamentsSize, lineupsSize))
        tournaments.forEach {
            items.add(TimeLineLineItem(it.first))
            if(it.second.isEmpty()) {
                when(index) {
                    tournaments.size - 1 -> items.add(EmptyTimeLineItem(MaterialTimelineView.POSITION_LAST))
                    else -> items.add(EmptyTimeLineItem(MaterialTimelineView.POSITION_MIDDLE))
                }
            }
            else {
                when (index) {
                    tournaments.size - 1 -> items.add(TimelineBottomView(it.first, it.second))
                    else -> items.add(TimelineMiddleView(it.first, it.second))
                }
            }

            index++
        }
    }
}
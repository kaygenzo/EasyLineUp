package com.telen.easylineup.dashboard

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.dashboard.models.ITileData
import com.telen.easylineup.dashboard.tiles.LastLineupTile
import com.telen.easylineup.dashboard.tiles.MostUsedPlayerTile
import com.telen.easylineup.dashboard.tiles.TeamSizeTile

const val TYPE_LAST_LINEUP = 0
const val TYPE_TEAM_SIZE = 1
const val TYPE_MOST_USED_PLAYER = 2

interface TileClickListener {
    fun onTileClicked(type: Int)
}

class DashboardTileAdapter(val list: List<ITileData>, val tileClickListener: TileClickListener): RecyclerView.Adapter<DashboardTileAdapter.TileViewHolder>() {

    inner class TileViewHolder(val view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        return when(viewType) {
            TYPE_TEAM_SIZE -> {
                TileViewHolder(TeamSizeTile(parent.context))
            }
            TYPE_MOST_USED_PLAYER -> {
                TileViewHolder(MostUsedPlayerTile(parent.context))
            }
            TYPE_LAST_LINEUP -> {
                TileViewHolder(LastLineupTile(parent.context))
            }
            else -> {
                throw NotImplementedError()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        val data = list[position]
        return data.getType()
    }

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        val element = list[position]
        when {
            holder.view is TeamSizeTile -> holder.view.bind(element)
            holder.view is MostUsedPlayerTile -> holder.view.bind(element)
            holder.view is LastLineupTile -> holder.view.bind(element)
        }
        holder.view.setOnClickListener {
            tileClickListener.onTileClicked(element.getType())
        }
    }
}
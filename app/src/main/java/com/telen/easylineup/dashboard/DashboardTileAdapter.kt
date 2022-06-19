package com.telen.easylineup.dashboard

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.dashboard.tiles.LastLineupTile
import com.telen.easylineup.dashboard.tiles.MostUsedPlayerTile
import com.telen.easylineup.dashboard.tiles.PlayerNumberSearchTile
import com.telen.easylineup.dashboard.tiles.TeamSizeTile
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DashboardTile
import java.util.*

const val INDEX_SEND_MESSAGES = 0
const val INDEX_SEND_EMAILS = 1
const val INDEX_SEND_OTHER = 2

class DashboardTileAdapter(
    private val tileClickListener: TileClickListener,
    var inEditMode: Boolean = false
) : ListAdapter<DashboardTile, DashboardTileAdapter.TileViewHolder>(DiffCallback()),
    ItemTouchHelperAdapter {

    private class DiffCallback : DiffUtil.ItemCallback<DashboardTile>() {
        override fun areItemsTheSame(oldItem: DashboardTile, newItem: DashboardTile) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DashboardTile, newItem: DashboardTile) =
            oldItem == newItem
    }

    inner class TileViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        return when (viewType) {
            Constants.TYPE_TEAM_SIZE -> {
                TileViewHolder(TeamSizeTile(parent.context))
            }
            Constants.TYPE_MOST_USED_PLAYER -> {
                TileViewHolder(MostUsedPlayerTile(parent.context))
            }
            Constants.TYPE_LAST_LINEUP -> {
                TileViewHolder(LastLineupTile(parent.context))
            }
            Constants.TYPE_LAST_PLAYER_NUMBER -> {
                TileViewHolder(PlayerNumberSearchTile(parent.context))
            }
            else -> {
                throw NotImplementedError()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).data?.getType() ?: 0
    }

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        getItem(position).data?.let { data ->
            with(holder.view) {
                when (this) {
                    is TeamSizeTile -> bind(data, inEditMode, tileClickListener)
                    is MostUsedPlayerTile -> bind(data, inEditMode)
                    is LastLineupTile -> bind(data, inEditMode)
                    is PlayerNumberSearchTile -> bind(data, inEditMode, tileClickListener)
                }
                setOnClickListener {
                    if (!inEditMode) {
                        tileClickListener.onTileClicked(data.getType(), data)
                    }
                }
                setOnLongClickListener {
                    tileClickListener.onTileLongClicked(data.getType())
                    true
                }
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (inEditMode) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(currentList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(currentList, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
        }
    }
}
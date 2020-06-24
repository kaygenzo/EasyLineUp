package com.telen.easylineup.dashboard

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.dashboard.tiles.LastLineupTile
import com.telen.easylineup.dashboard.tiles.MostUsedPlayerTile
import com.telen.easylineup.dashboard.tiles.ShakeBetaTile
import com.telen.easylineup.dashboard.tiles.TeamSizeTile
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.tiles.ITileData
import java.util.*

interface TileClickListener {
    fun onTileClicked(type: Int, data: ITileData)
    fun onTileLongClicked(type: Int)
}

class DashboardTileAdapter(private val list: List<ITileData>, private val tileClickListener: TileClickListener, var inEditMode: Boolean = false):
        RecyclerView.Adapter<DashboardTileAdapter.TileViewHolder>(), ItemTouchHelperAdapter {

    inner class TileViewHolder(val view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        return when(viewType) {
            Constants.TYPE_TEAM_SIZE -> {
                TileViewHolder(TeamSizeTile(parent.context))
            }
            Constants.TYPE_MOST_USED_PLAYER -> {
                TileViewHolder(MostUsedPlayerTile(parent.context))
            }
            Constants.TYPE_LAST_LINEUP -> {
                TileViewHolder(LastLineupTile(parent.context))
            }
            Constants.TYPE_SHAKE_BETA -> {
                TileViewHolder(ShakeBetaTile(parent.context))
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
        when(holder.view) {
            is TeamSizeTile -> holder.view.bind(element, inEditMode)
            is MostUsedPlayerTile -> holder.view.bind(element, inEditMode)
            is LastLineupTile -> holder.view.bind(element, inEditMode)
            is ShakeBetaTile -> holder.view.bind(element, inEditMode)
        }
        holder.view.setOnClickListener {
            tileClickListener.onTileClicked(element.getType(), element)
        }
        holder.view.setOnLongClickListener {
            tileClickListener.onTileLongClicked(element.getType())
            true
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if(inEditMode) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(list, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(list, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
        }
    }
}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
}

class DashboardTileTouchCallback(val adapter: DashboardTileAdapter): ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

}
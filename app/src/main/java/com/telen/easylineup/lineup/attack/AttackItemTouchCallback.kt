package com.telen.easylineup.lineup.attack

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class AttackItemTouchCallback(val adapter: BattingOrderAdapter): ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = when(viewHolder.adapterPosition) {
            in 0..8 -> ItemTouchHelper.UP or ItemTouchHelper.DOWN
            else -> 0
        }
        Timber.d("getMovementFlags=$dragFlags position=${viewHolder.adapterPosition}")
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        adapter.onMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> adapter.onDragStart()
            ItemTouchHelper.ACTION_STATE_IDLE -> adapter.onIdle()
        }
        super.onSelectedChanged(viewHolder, actionState)
    }
}